package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;
import com.exerciting.Exerciting.Domain.game.dto.GameCrawlRequestDto;
import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.stadium.repository.StadiumRepository;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.Domain.team.repository.TeamRepository;
import com.exerciting.Exerciting.Exception.CrawlingException;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KboDateFetcher {
    private final RestTemplate restTemplate;
    private final CrawlerHelper crawlerHelper;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private static final String URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    public KboDateFetcher(RestTemplate restTemplate, CrawlerHelper crawlerHelper, TeamRepository teamRepository,StadiumRepository stadiumRepository,
                          GameRepository gameRepository) {
        this.restTemplate = restTemplate;
        this.crawlerHelper = crawlerHelper;
        this.teamRepository = teamRepository;
        this.stadiumRepository = stadiumRepository;
        this.gameRepository = gameRepository;
    }
    @PostConstruct
    public List<GameCrawlRequestDto> fetch() {
        WebDriver driver = null;
        List<GameCrawlRequestDto> result = new ArrayList<>();
    try {
        driver = crawlerHelper.createWebDriver();
        driver.get(URL);
        Thread.sleep(2000);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement seriesSelect = driver.findElement(By.id("ddlSeries"));
        Select select = new Select(seriesSelect);
        select.selectByValue("0,9,6");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("tblScheduleList")
        ));
        List<WebElement> rows = driver.findElements(
                By.cssSelector("#tblScheduleList tbody tr")
        );
        String currentDate = "";
        for (WebElement row : rows) {
            try {

                List<WebElement> cells = row.findElements(By.tagName("td"));
                List<WebElement> dayCell = row.findElements(By.cssSelector("td.day"));

                String time = "";
                String home = "";
                String away = "";
                String stadium = "";
                if (!dayCell.isEmpty()) {
                    currentDate = cells.get(0).getText().trim();
                    time = cells.get(1).getText().trim();
                    WebElement playCell = cells.get(2);
                    List<WebElement> teams = playCell.findElements(By.xpath("./span"));
                    away = teams.get(0).getText().trim();
                    home = teams.get(1).getText().trim();
                    stadium     = cells.get(7).getText().trim();
                    log.info("크롤링 완료(날짜 가져옴) : {}, {}, {}, {}", time,home,away,stadium);
                } else {
                    time        = cells.get(0).getText().trim();
                    WebElement playCell = cells.get(1);
                    List<WebElement> spans = playCell.findElements(By.xpath("./span"));
                    away = spans.get(0).getText().trim();
                    home = spans.get(1).getText().trim();
                    stadium     = cells.get(6).getText().trim();
                    log.info("크롤링 완료(날짜 가져옴X) : {}, {}, {}, {}", time,home,away,stadium);
                }
                if (currentDate.isBlank() || home.isBlank() || away.isBlank()) continue;

                GameCrawlRequestDto dto = GameCrawlRequestDto.builder()
                        .homeTeam(home)
                        .awayTeam(away)
                        .stadiumName(stadium)
                        .sportType(SportType.BASEBALL.name())
                        .gameStartTime(getCleanDate(currentDate, time))
                        .build();

                result.add(dto);
                saveGame(dto);
                Thread.sleep(200);
            } catch (Exception e) {
                log.warn("행 파싱 실패, 스킵: {}", e.getMessage());
            }
        }

        log.info("KBO 경기 일정 크롤링 완료 - {}건", result.size());

    } catch (Exception e) {
        throw new CrawlingException("KBO 일정 크롤링 실패", e);
    } finally {
        if (driver != null) {
            driver.quit(); // 브라우저 반드시 닫기
        }
    }

        return result;
    }
    private void saveGame(GameCrawlRequestDto dto) {
        Team homeTeam = teamRepository.findByShortName(dto.getHomeTeam()).orElse(null);
        Team awayTeam = teamRepository.findByShortName(dto.getAwayTeam()).orElse(null);
        Stadium stadium = stadiumRepository.findByShortName(dto.getStadiumName()).orElse(null);

        if (homeTeam == null || awayTeam == null || stadium == null) {
            log.warn("팀 또는 경기장 미존재 - 홈:{} 원정:{} 경기장:{}",
                    dto.getHomeTeam(), dto.getAwayTeam(), dto.getStadiumName());
            return;
        }

        boolean exists = gameRepository.existsByHomeTeamAndAwayTeamAndGameStartTime(homeTeam, awayTeam, dto.getGameStartTime());
        if (exists) {
            log.debug("이미 저장된 경기 스킵: {} vs {}", dto.getHomeTeam(), dto.getAwayTeam());
            return;
        }

        Game game = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(dto.getGameStartTime())
                .gameStatus(GameStatus.BEFORE)
                .build();

        gameRepository.save(game);
        log.info("경기 저장 완료: {} vs {}", dto.getHomeTeam(), dto.getAwayTeam());
    }
    private LocalDateTime getCleanDate(String date, String time) {
        String cleanDate = date.replaceAll("\\(.*?\\)", "").trim();
        log.info("cleanDate: '{}', time: '{}'", cleanDate, time);
        int year = LocalDateTime.now().getYear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        String startDate = year + "." + cleanDate + " " + time;
        log.info("final starttime : {}", startDate);
        return LocalDateTime.parse(startDate, formatter);
    }
}
