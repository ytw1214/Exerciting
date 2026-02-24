package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;
import com.exerciting.Exerciting.Infrastructure.configuration.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KboDateFetcher {
    private final RestTemplate restTemplate;

    public KboDateFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /*
    public void getdate() {
        String url = "https://www.koreabaseball.com/ws/Main.asmx/GetKboGameDate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "leId=1&srIdList=1&seasonId=2026&gameMonth=05";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(url, entity, String.class);
        System.out.println(response);
    }


     */
}
