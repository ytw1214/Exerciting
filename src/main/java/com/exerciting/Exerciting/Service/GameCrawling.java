package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Game;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GameCrawling {

    public List<Game> crawling(String command) {
        String[] arr = command.split(" ");
        System.setProperty("webdriver.chrome.driver","C:\\Users\\부안\\Desktop\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        WebElement year = driver.findElement(By.id("ddlYear"));
        Select selectYear = new Select(year);
        WebElement month = driver.findElement(By.id("ddlMonth"));
        Select selectMonth = new Select(month);
        WebElement gameType = driver.findElement(By.id("ddlSeries"));
        Select selectType = new Select(gameType);

        selectYear.selectByValue(arr[0]);
        selectYear.selectByValue(arr[1]);
        selectYear.selectByValue(arr[2]);


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        //wait.until(ExpectedConditions.pre)
        List<Game> gameList = new ArrayList<>();

        try {
            driver.get("https://www.koreabaseball.com/Schedule/Schedule.aspx#");
            driver.findElement(By.id("ddlYear"));
        }
    }
}
