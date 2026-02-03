package com.exerciting.Exerciting.Service.Crawling;

import com.exerciting.Exerciting.Entity.Player;
import com.exerciting.Exerciting.Entity.SportType;
import com.exerciting.Exerciting.Entity.TeamRank;
import com.exerciting.Exerciting.Repository.PlayerRepository;
import com.exerciting.Exerciting.Repository.TeamRankRepository;
import com.exerciting.Exerciting.dto.Player.PlayerCrawlDto;
import com.exerciting.Exerciting.dto.Team.TeamRankCrawlDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BaseballCrawlingService extends CrawlingService<TeamRankCrawlDto> {
    private static String KBORankUrl = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
    private static String KBOScheduleUrl = "https://www.koreabaseball.com/Schedule/Schedule.aspx";
    private static String playerUrl = "https://www.koreabaseball.com/Record/Player/HitterBasic/Basic1.aspx?sort=HRA_RT";
    //private final GameRepository gameRepository;
    private final TeamRankRepository teamRankRepository;
    private final PlayerRepository playerRepository;

    public BaseballCrawlingService(TeamRankRepository teamRankRepository, PlayerRepository playerRepository) {
        this.teamRankRepository = teamRankRepository;
        this.playerRepository = playerRepository;
    }
    @PostConstruct
    public void init() {
        org.springframework.util.StopWatch stopWatch = new org.springframework.util.StopWatch();
        try {
            setSSL();
            log.info("인증서 우회중");
            stopWatch.start();
            //getRank();
            this.execute();
            stopWatch.stop();
            log.info("총 소요시간 {}ms", stopWatch.getTotalTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getTargetUrl() {
        return KBORankUrl;
    }
    @Override
    protected void saveAll(List<TeamRankCrawlDto> data) {
        List<TeamRank> list = convertToEntity(data);
        teamRankRepository.saveAll(convertToEntity(data));
        log.info("kbo {}개 데이터 저장 완료",list.size());
    }
    public List<TeamRankCrawlDto> getRank() {
        try {
            Document doc = Jsoup.connect(KBORankUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.koreabaseball.com")
                    .timeout(10000)
                    .get();

            List<TeamRankCrawlDto> ranking = parse(doc);
            log.info("크롤링 완 : {}개", ranking.size());
            ranking.forEach(r -> log.info("팀 순위 정보: {}", r));
            List<TeamRank> list = convertToEntity(ranking);
            teamRankRepository.saveAll(list);


            log.info("DB 저장 완료! {}건 저장됨",list.size());
            return ranking;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("순위 can't");
        }
    }
    protected List<TeamRankCrawlDto> parse(Document doc) {
        List<TeamRankCrawlDto> rankings = new ArrayList<>();
        try {
            Element rankTable = doc.selectFirst("table[summary*='순위']");
            if (rankTable != null) {
                Elements rows = rankTable.select("tbody tr");
                log.info("테이블 검색 완료~!~!~!");
                for (Element row : rows) {
                    Elements cells = row.select("td");

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
                    log.info("데이터 삽입 완료!");
                }
            }
        }catch(Exception e) {
                log.error("크롤링 실패 : {}", e.getMessage());
            }
            return rankings;
        }

        private List<TeamRank> convertToEntity(List<TeamRankCrawlDto> list) {
            return list.stream()
                    .map(TeamRankCrawlDto::toEntity)
                    .toList();
        }

    protected List<PlayerCrawlDto> parse_player(Document doc) {
        List<PlayerCrawlDto> players = new ArrayList<>();
        try {
            Element playerTable = doc.selectFirst("table[summary*='기본기록']");
            if (playerTable != null) {
                Elements rows = playerTable.select("tbody tr");
                log.info("테이블 검색 완료~!~!~!");
                for (Element row : rows) {
                    Elements cells = row.select("td");

                    players.add(PlayerCrawlDto.builder()
                            .name(cells.get(1).text())
                            .teamName(cells.get(2).text())
                            .sportType(SportType.BASEBALL)
                            .build());
                    log.info("데이터 삽입 완료!");
                }
            }
        }catch(Exception e) {
            log.error("크롤링 실패 : {}", e.getMessage());
        }
        return players;
    }
    public List<PlayerCrawlDto> getPlayer() {
        try {
            Document doc = Jsoup.connect(playerUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.koreabaseball.com")
                    .timeout(10000)
                    .get();

            List<PlayerCrawlDto> players = parse_player(doc);
            log.info("크롤링 완 : {}개", players.size());
            players.forEach(r -> log.info("선수 정보: {}", r));
            List<Player> list = convertToEntity_player(players);
            playerRepository.saveAll(list);


            log.info("DB 저장 완료! {}건 저장됨",list.size());
            return players;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("순위 can't");
        }
    }
    private List<Player> convertToEntity_player(List<PlayerCrawlDto> list) {
        return list.stream()
                .map(PlayerCrawlDto::toEntity)
                .toList();
    }
    protected void saveAll_player(List<PlayerCrawlDto> data) {
        List<Player> list = convertToEntity_player(data);
        playerRepository.saveAll(list);
        log.info("kbo {}개 데이터 저장 완료",list.size());
    }


}
