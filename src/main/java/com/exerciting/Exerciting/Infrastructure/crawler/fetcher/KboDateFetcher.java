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

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement seriesSelect = driver.findElement(By.id("ddlSeries"));
        Select select = new Select(seriesSelect);
        select.selectByValue("0,9,6");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("table.tbl-type06 tbody tr")
        ));

        List<WebElement> rows = driver.findElements(
                By.cssSelector("table.tbl-type06 tbody tr")
        );

        for (WebElement row : rows) {
            try {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 5) continue;

                String date     = cells.get(0).getText().trim(); // 날짜
                String time     = cells.get(1).getText().trim(); // 시간
                String home     = cells.get(2).getText().trim(); // 홈팀
                String away     = cells.get(3).getText().trim(); // 원정팀
                String stadium  = cells.get(4).getText().trim(); // 경기장

                if (date.isBlank() || home.isBlank() || away.isBlank()) continue;

                GameCrawlRequestDto dto = GameCrawlRequestDto.builder()
                        .homeTeam(home)
                        .awayTeam(away)
                        .stadiumName(stadium)
                        .sportType(SportType.BASEBALL.name())
                        .gameStartTime(date + " " + time)
                        .build();

                result.add(dto);
                saveGame(dto);

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
        Team homeTeam = teamRepository.findByName(dto.getHomeTeam()).orElse(null);
        Team awayTeam = teamRepository.findByName(dto.getAwayTeam()).orElse(null);
        List<Stadium> stadiums = stadiumRepository.findByNameContaining(dto.getStadiumName());

        if (homeTeam == null || awayTeam == null || stadiums.isEmpty()) {
            log.warn("팀 또는 경기장 미존재 - 홈:{} 원정:{} 경기장:{}",
                    dto.getHomeTeam(), dto.getAwayTeam(), dto.getStadiumName());
            return;
        }

        Stadium stadium = stadiums.get(0);

        // 같은 경기 중복 저장 방지
        boolean exists = gameRepository.findByTeamName(homeTeam.getName())
                .stream()
                .anyMatch(g -> g.getHomeTeam().getName().equals(homeTeam.getName())
                        && g.getAwayTeam().getName().equals(awayTeam.getName())
                        && g.getStadium().getId().equals(stadium.getId()));

        if (exists) {
            log.debug("이미 저장된 경기 스킵: {} vs {}", dto.getHomeTeam(), dto.getAwayTeam());
            return;
        }

        Game game = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(parseDateTime(dto.getGameStartTime()))
                .gameStatus(GameStatus.BEFORE)
                .build();

        gameRepository.save(game);
        log.info("경기 저장 완료: {} vs {}", dto.getHomeTeam(), dto.getAwayTeam());
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // "03.28 18:30" 형식 대응
            int year = LocalDateTime.now().getYear();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");
            return LocalDateTime.parse(dateTimeStr, formatter).withYear(year);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateTimeStr);
            return LocalDateTime.now();
        }
    }
}
