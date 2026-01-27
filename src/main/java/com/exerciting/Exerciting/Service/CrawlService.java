package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.TeamRank;
import com.exerciting.Exerciting.dto.Game.GameCrawlRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
@Slf4j
public abstract class CrawlService {
    //크롤링 인증서 코드
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
    protected abstract String getTargetUrl();
    protected abstract List<GameCrawlRequestDto> parse(Document doc);
    protected abstract void saveAll(List<GameCrawlRequestDto> data);

    // 공통 실행 흐름 (기존 getRank의 구조를 가져옴)
    public List<GameCrawlRequestDto> execute() {
        try {
            setSSL(); // 공통 인증서 설정

            Document doc = Jsoup.connect(getTargetUrl()) // 자식의 URL 사용
                    .userAgent("Mozilla/5.0 ...")
                    .timeout(10000)
                    .get();

            List<GameCrawlRequestDto> dataList = parse(doc); // 자식의 파싱 로직 사용

            log.info("크롤링 완 : {}개", dataList.size());
            saveAll(dataList); // 자식의 레포지토리 사용

            log.info("DB 저장 완료!");
            return dataList;

        } catch (Exception e) {
            log.error("크롤링 중 에러 발생", e);
            throw new RuntimeException("크롤링 실패");
        }
    }
}
