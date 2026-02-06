package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Exception.CrawlingException;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
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
public class KboRankFetcher implements RankFetcher {
    private final CrawlerHelper crawlerHelper;

    public KboRankFetcher(CrawlerHelper crawlerHelper) {
        this.crawlerHelper = crawlerHelper;
    }

    public List<TeamRankCrawlDto> fetch() {
        String url = SportType.BASEBALL.getRankUrl();
        Document document = crawlerHelper.createSafeConnection(url);
        List<TeamRankCrawlDto> list = new ArrayList<>();
        /*
        Document document = crawlerHelper.createSafeConnection(url);

        return document;

         */
        return list;
    }
    //
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
                    log.info("데이터 사이즈 : {}개",cells.size());
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
                            .winRate(new BigDecimal(cells.get(6).text()))
                            .gamesBehind(new BigDecimal(cells.get(7).text()))
                            .crawledAt(LocalDateTime.now())
                            .dataSource("KBO 공식 홈페이지")
                            .build());
                }
            }
        }catch(Exception e) {
            throw new CrawlingException("크롤링 오류",e);
        }
        return rankings;
    }
    public boolean supports(SportType sportType) {
        return sportType == SportType.BASEBALL;
    }


}
