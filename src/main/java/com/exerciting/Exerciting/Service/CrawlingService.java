package com.exerciting.Exerciting.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class CrawlingService {
    @PostConstruct
    public static void getRank() {
        WebDriverManager.chromedriver().setup();
        String url = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
        WebDriver driver = new ChromeDriver();
        try {
            // 3. 페이지 접속
            driver.get(url);

            // 데이터 로딩을 기다리기 위해 잠시 대기 (3초)
            Thread.sleep(3000);

            // 4. 데이터 추출 (CSS 선택자 사용)
            // 셀레니엄의 findElements는 Jsoup의 select와 비슷합니다.
            List<WebElement> rows = driver.findElements(By.cssSelector("tbody"));

            for (WebElement row : rows) {
                String content = row.getText();
                String[] data = content.split(" ");
                String rank = data[0];      // 순위
                String teamName = data[1];  // 팀명
                String win = data[3];       // 승
                String loss = data[4];      // 패
                String draw = data[5];      // 무
                String winRate = data[6];   // 승률

                log.info("{}위: {}, {}승 {}패 {}무 (승률: {})",
                        rank, teamName, win, loss, draw, winRate);
            }

        } catch (Exception e) {
            log.error("셀레니엄 크롤링 중 에러: {}", e.getMessage());
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
    }
}
