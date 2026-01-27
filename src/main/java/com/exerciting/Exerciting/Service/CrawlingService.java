package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.TeamRank;
import com.exerciting.Exerciting.Repository.TeamRankRepository;
import com.exerciting.Exerciting.dto.Game.GameSearchRequestDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CrawlingService {
    private static String KBORankUrl = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
    private static String KBOScheduleUrl = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    private final TeamRankRepository teamRankRepository;

    public CrawlingService(TeamRankRepository teamRankRepository) {
        this.teamRankRepository = teamRankRepository;
    }
    public void init() {
        try {
            setSSL();
            log.info("인증서 우회중");
            getRank();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //kbo 리그 순위 크롤링
    public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) { return true; }
        });
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    public List<TeamRank> getRank() {
        try {
            Document doc = Jsoup.connect(KBORankUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.koreabaseball.com")
                    .timeout(10000)
                    .get();

            List<TeamRank> ranking = parseRankings(doc);
            log.info("크롤링 완 : {}개", ranking.size());
            ranking.forEach(r -> log.info("팀 순위 정보: {}", r));
            teamRankRepository.saveAll(ranking);

            log.info("DB 저장 완료!");
            return ranking;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("순위 can't");
        }
    }

    private List<TeamRank> parseRankings(Document doc) {
        List<TeamRank> rankings = new ArrayList<>();


        try {
            Element rankTable = doc.selectFirst("table[summary*='순위']");
            if (rankTable != null) {
                Elements rows = rankTable.select("tbody tr");
                log.info("테이블 검색 완료~!~!~!");
                for (Element row : rows) {
                    Elements cells = row.select("td");

                    rankings.add(TeamRank.builder()
                            .teamRank(Integer.parseInt(cells.get(0).text()))      // 순위
                            .teamName(cells.get(1).text())                   // 팀명
                            .games(Integer.parseInt(cells.get(2).text()))
                            .wins(Integer.parseInt(cells.get(3).text()))      // 승
                            .losses(Integer.parseInt(cells.get(4).text()))     // 패
                            .draws(Integer.parseInt(cells.get(5).text()))     // 무
                            .winRate(Double.parseDouble(cells.get(6).text())) // 승률
                            .gamesBehind(cells.get(7).text())
                            .crawledAt(LocalDateTime.now())
                            .dataSource("KBO 공식 홈페이지")
                            .build());
                    log.info("데이터 삽입 완료!");
                }
            }
        }catch(Exception e) {
                log.error("크롤링 실패 : {}", e.getMessage());
            }
            return rankings;
        }
        /*
        // KBO 순위 테이블 파싱
        Elements rows = doc.select("tbody tr");

        for (Element row : rows) {
            Elements cols = row.select("td");

            if (cols.isEmpty()) continue;

            try {
                TeamRank ranking = TeamRank.builder()
                        .teamRank(parseIntSafely(cols.get(0).text()))
                        .teamName(cols.get(1).text().trim())
                        .games(parseIntSafely(cols.get(2).text()))
                        .wins(parseIntSafely(cols.get(3).text()))
                        .losses(parseIntSafely(cols.get(4).text()))
                        .draws(parseIntSafely(cols.get(5).text()))
                        .winRate(parseDoubleSafely(cols.get(6).text()))
                        .gamesBehind(cols.get(7).text().trim())
                        .dataSource("KBO 공식 홈페이지")
                        .crawledAt(LocalDateTime.now())
                        .build();

                rankings.add(ranking);

            } catch (Exception e) {
                log.warn("행 파싱 실패: {}", row.text(), e);
            }
        }

        return rankings;
    }


         */
    /*
    private List<GameSearchRequestDto> getGameSchedule() {
        try {
            Document doc = Jsoup.connect(KBOScheduleUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.koreabaseball.com")
                    .timeout(10000)
                    .get();

            List<GameSearchRequestDto> ranking = parseGames(doc);
            log.info("크롤링 완 : {}개", ranking.size());
            ranking.forEach(r -> log.info("팀 순위 정보: {}", r));
            teamRankRepository.saveAll(ranking);

            log.info("DB 저장 완료!");
            return ranking;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("순위 can't");
        }
    }
    public List<KboCrawlRequest> crawlSchedule(int year, int month) {
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://www.koreabaseball.com/Schedule/Schedule.aspx");

            // 1. 연도 선택 (2026년)
            new Select(driver.findElement(By.id("ddlYear"))).selectByValue(String.valueOf(year));
            // 2. 월 선택
            new Select(driver.findElement(By.id("ddlMonth"))).selectByValue(String.format("%02d", month));

            // 3. 데이터 로딩 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("tbl-schedule")));

            // 4. Jsoup 파싱 (속도를 위해 HTML만 넘김)
            Document doc = Jsoup.parse(driver.getPageSource());
            Elements rows = doc.select(".tbl-schedule tbody tr");

            return rows.stream().map(row -> {
                // 날짜, 시간, 팀, 구장 추출 로직 수행
                return new KboCrawlRequest(...);
            }).toList();

        } finally {
            driver.quit(); // 메모리 누수 방지 (중요!)
        }
    }
    private List<GameSearchRequestDto> parseGames(Document doc) {
        List<TeamRank> rankings = new ArrayList<>();

        // KBO 순위 테이블 파싱
        Elements rows = doc.select("table.tData tbody tr");

        for (Element row : rows) {
            Elements cols = row.select("td");

            if (cols.isEmpty()) continue;

            try {
                TeamRank ranking = TeamRank.builder()
                        .teamRank(parseIntSafely(cols.get(0).text()))
                        .teamName(cols.get(1).text().trim())
                        .games(parseIntSafely(cols.get(2).text()))
                        .wins(parseIntSafely(cols.get(3).text()))
                        .losses(parseIntSafely(cols.get(4).text()))
                        .draws(parseIntSafely(cols.get(5).text()))
                        .winRate(parseDoubleSafely(cols.get(6).text()))
                        .gamesBehind(cols.get(7).text().trim())
                        .dataSource("KBO 공식 홈페이지")
                        .crawledAt(LocalDateTime.now())
                        .build();
                GameSearchRequestDto gameSearchRequestDto = GameSearchRequestDto.builder()
                        .gameStartTime()
                        .build()
                rankings.add(ranking);

            } catch (Exception e) {
                log.warn("행 파싱 실패: {}", row.text(), e);
            }
        }

        return rankings;
    }


     */
}
