package com.exerciting.Exerciting.Infrastructure.crawler;

import com.exerciting.Exerciting.Exception.CrawlingException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class CrawlerHelper {


    public Document createSafeConnection(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .referrer("https://www.koreabaseball.com/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Cache-Control", "max-age=0")
                    .timeout(10000)
                    .get();
        } catch (Exception e) {
            throw new CrawlingException("연걸 실패" + url, e);
        }
    }

}
