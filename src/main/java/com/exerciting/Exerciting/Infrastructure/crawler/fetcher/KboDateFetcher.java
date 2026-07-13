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
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.UnsupportedYearCrawlException;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import com.exerciting.Exerciting.Infrastructure.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KboDateFetcher {
    private final CrawlerHelper crawlerHelper;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private static final String URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";
    private static final int MIN_SUPPORTED_YEAR = 2000;
    /*
    @Async
    public void fetch(String year) {
        WebDriver driver = null;
        List<GameCrawlRequestDto> result = new ArrayList<>();
        if(Integer.parseInt(year) <= 2000 || Integer.parseInt(year) > LocalDateTime.now().getYear()) {
            log.warn("해당 연도를 찾을 수 없습니다. 크롤링을 종료합니다.");
            return;
        }
    try {
        driver = crawlerHelper.createWebDriver();
        driver.get(URL);
        WebDriverWait initialWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        initialWait.until(ExpectedConditions.elementToBeClickable(By.id("ddlYear")));

        String[] monthlist = {"04", "05", "06"};
        WebElement yearSelect = driver.findElement(By.id("ddlYear"));
        Select selectYear = new Select(yearSelect);
        selectYear.selectByValue(year);

        log.info("==== 크롤링 작업 시작 ====");
        long startTime = System.currentTimeMillis();
        for (String month : monthlist) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement monthSelect = driver.findElement(By.id("ddlMonth"));
            Select selectMonth = new Select(monthSelect);
            selectMonth.selectByValue(month);
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
                        stadium = cells.get(7).getText().trim();
                        log.info("크롤링 완료(날짜 가져옴) : {}, {}, {}, {}", time, home, away, stadium);
                    } else {
                        time = cells.get(0).getText().trim();
                        WebElement playCell = cells.get(1);
                        List<WebElement> spans = playCell.findElements(By.xpath("./span"));
                        away = spans.get(0).getText().trim();
                        home = spans.get(1).getText().trim();
                        stadium = cells.get(6).getText().trim();
                        log.info("크롤링 완료(날짜 가져옴X) : {}, {}, {}, {}", time, home, away, stadium);
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
            log.info("{}월 크롤링 완료",month);
    }
        long endTime = System.currentTimeMillis(); // 종료 시간 (ms)
        long duration = endTime - startTime; // 소요 시간 계산

        log.info("==== 크롤링 작업 종료 ====");
        log.info("총 소요 시간: {} ms (약 {}초)", duration, (duration / 1000.0));
        log.info("KBO 경기 일정 크롤링 완료 - {}건", result.size());
    } catch (Exception e) {
        log.error("크롤링 오류" + e.getMessage());
        throw new CrawlingException();
    } finally {
        if (driver != null) {
            driver.quit();
        }
    }
    }
    */


    public List<GameCrawlRequestDto> fetch(String year) {

        // ── 연도 검증: guard clause + try-catch ──
        int parsedYear;
        try {
            parsedYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            throw new UnsupportedYearCrawlException(
                    "지원하지 않는 연도입니다."
            );
        }
        int currentYear = LocalDateTime.now().getYear();
        if (parsedYear < MIN_SUPPORTED_YEAR || parsedYear > currentYear) {
            throw new UnsupportedCrawlYearException(
                    String.format("지원하지 않는 연도입니다. %d년 ~ %d년까지 조회 가능합니다.",
                            MIN_SUPPORTED_YEAR, currentYear)
            );
        }

        WebDriver driver = null;
        List<GameCrawlRequestDto> result = new ArrayList<>();

        try {
            driver = crawlerHelper.createWebDriver();
            driver.get(URL);

            WebDriverWait initialWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            initialWait.until(ExpectedConditions.elementToBeClickable(By.id("ddlYear")));

            String[] monthlist = {"04", "05", "06"};
            WebElement yearSelect = driver.findElement(By.id("ddlYear"));
            Select selectYear = new Select(yearSelect);
            selectYear.selectByValue(year);

            log.info("==== 크롤링 작업 시작 ====");
            long startTime = System.currentTimeMillis();

            for (String month : monthlist) {
                // ③ 갱신 전 테이블 요소 참조 저장 (staleness 체크용)
                WebElement oldTable = driver.findElement(By.id("tblScheduleList"));

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement monthSelect = driver.findElement(By.id("ddlMonth"));
                new Select(monthSelect).selectByValue(month);
                WebElement seriesSelect = driver.findElement(By.id("ddlSeries"));
                new Select(seriesSelect).selectByValue("0,9,6");

                // ③ "존재하는지"가 아니라 "갱신됐는지"를 확인
                wait.until(ExpectedConditions.stalenessOf(oldTable));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tblScheduleList")));

                List<WebElement> rows = driver.findElements(
                        By.cssSelector("#tblScheduleList tbody tr")
                );
                String currentDate = "";

                for (WebElement row : rows) {
                    try {
                        List<WebElement> cells = row.findElements(By.tagName("td"));
                        List<WebElement> dayCell = row.findElements(By.cssSelector("td.day"));

                        String time;
                        String home;
                        String away;
                        String stadium;
                        if (!dayCell.isEmpty()) {
                            currentDate = cells.get(0).getText().trim();
                            time = cells.get(1).getText().trim();
                            WebElement playCell = cells.get(2);
                            List<WebElement> teams = playCell.findElements(By.xpath("./span"));
                            away = teams.get(0).getText().trim();
                            home = teams.get(1).getText().trim();
                            stadium = cells.get(7).getText().trim();
                        } else {
                            time = cells.get(0).getText().trim();
                            WebElement playCell = cells.get(1);
                            List<WebElement> spans = playCell.findElements(By.xpath("./span"));
                            away = spans.get(0).getText().trim();
                            home = spans.get(1).getText().trim();
                            stadium = cells.get(6).getText().trim();
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
                        // ② Thread.sleep(200) 삭제 — 서버 요청 없는 로컬 DOM 읽기라 불필요

                    } catch (Exception e) {
                        log.warn("행 파싱 실패, 스킵: {}", e.getMessage());
                    }
                }
                log.info("{}월 크롤링 완료", month);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("==== 크롤링 작업 종료 ====");
            log.info("총 소요 시간: {} ms (약 {}초)", duration, (duration / 1000.0));
            log.info("KBO 경기 일정 크롤링 완료 - {}건", result.size());

            return result; // 0건이어도 정상 반환 (예외 아님)

        } catch (Exception e) {
            log.error("크롤링 오류" + e.getMessage());
            throw new CrawlingException();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
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
