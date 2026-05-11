package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Exception.CrawlingException;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
public class KboRankFetcher implements Fetcher {
    private final CrawlerHelper crawlerHelper;

    public KboRankFetcher(CrawlerHelper crawlerHelper) {
        this.crawlerHelper = crawlerHelper;
    }

    public List<TeamRankCrawlDto> fetch() {
        String url = SportType.BASEBALL.getRankUrl();
        Document document = crawlerHelper.createSafeConnection(url);
        return parseKboRank(document);
    }

    private List<TeamRankCrawlDto> parseKboRank(Document document) {
        List<TeamRankCrawlDto> rankings = new ArrayList<>();

        try {
            Element rankTable = document.selectFirst("table[summary*='순위']");
            if (rankTable != null) {
                Elements rows = rankTable.select("tbody tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if(cells.isEmpty()) {
                        log.error("데이터값이 유효하지 않습니다.");
                    }
                    if(cells.size() < 8) {
                        log.error("크롤링 데이터 부족");
                        continue;
                    }

                    rankings.add(TeamRankCrawlDto.builder()
                            .rank(Integer.parseInt(cells.get(0).text()))
                            .teamName(cells.get(1).text())
                            .games(Integer.parseInt(cells.get(2).text()))
                            .wins(Integer.parseInt(cells.get(3).text()))
                            .losses(Integer.parseInt(cells.get(4).text()))
                            .draws(Integer.parseInt(cells.get(5).text()))
                            .winRate(new BigDecimal(cells.get(6).text()).setScale(3))
                            .gamesBehind(new BigDecimal(cells.get(7).text()).setScale(3))
                            .crawledAt(LocalDateTime.now())
                            .dataSource("KBO 공식 홈페이지")
                            .build());
                    //System.out.println(rankings.get(rankings.size()-1));

                }
            }
        }catch(Exception e) {
            log.error("크롤링 오류" + e.getMessage());
            throw new CrawlingException();
        }
        log.info("크롤링 완료");
        for(TeamRankCrawlDto s : rankings) {
            System.out.println(s);
        }
        return rankings;
    }
    public boolean supports(SportType sportType) {
        return sportType == SportType.BASEBALL;
    }


}
