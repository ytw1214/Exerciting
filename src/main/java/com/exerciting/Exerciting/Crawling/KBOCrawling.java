package com.exerciting.Exerciting.Crawling;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class KBOCrawling {

    WebDriver driver = new ChromeDriver();
    String date = "20250617";
    String home = "KT";
    String away = "HT";
    String team = date+home+away+"0";
    /*
    try {
        driver.get("https://www.koreabaseball.com/Schedule/GameCenter/Main.aspx?gameDate=" + date + "&gameId=" + team + "&section=REVIEW");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));



    }

     */
}
