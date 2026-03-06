package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;
import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Infrastructure.configuration.Config;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Component
public class KboDateFetcher {
    private final RestTemplate restTemplate;
    private final CrawlerHelper crawlerHelper;

    public KboDateFetcher(RestTemplate restTemplate, CrawlerHelper crawlerHelper) {
        this.restTemplate = restTemplate;
        this.crawlerHelper = crawlerHelper;
    }

    public List<Game> parseDateFetcher() {
        WebDriver driver = null;
        String url = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

        try {
            driver = crawlerHelper.createWebDriver();
            driver.get(url);

            List<WebElement> dayElements = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("td.day"))
            );
        }
    }
}
