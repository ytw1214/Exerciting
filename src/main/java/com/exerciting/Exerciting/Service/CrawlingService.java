package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.TeamRank;
import com.exerciting.Exerciting.Repository.TeamRankRepository;
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
    private final TeamRankRepository teamRankRepository;

    public CrawlingService(TeamRankRepository teamRankRepository) {
        this.teamRankRepository = teamRankRepository;
    }
    @PostConstruct
    public void init() {
        try {
            setSSL();
            log.info("인증서 우회중");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
            String url = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
            Document doc = Jsoup.connect(url)
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
        /*
        try {
            String url = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
            Document doc = Jsoup.connect(url).get();
            Elements rows = doc.select("tbody tr"); // tbody 안의 행들을 선택

            for (Element row : rows) {
                // 여기서 출력을 해줘야 콘솔에 보입니다!
                System.out.println("데이터: " + row.text());
                // 또는
                //log.info("데이터: {}", row.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


         */

    private List<TeamRank> parseRankings(Document doc) {
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

                rankings.add(ranking);

            } catch (Exception e) {
                log.warn("행 파싱 실패: {}", row.text(), e);
            }
        }

        return rankings;
    }

    private int parseIntSafely(String text) {
        try {
            return Integer.parseInt(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDoubleSafely(String text) {
        try {
            String cleaned = text.trim();
            if (cleaned.startsWith(".")) {
                cleaned = "0" + cleaned;
            }
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
