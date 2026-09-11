package com.exerciting.Exerciting.Domain.matching.matching.scheduler;

import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 지금까지 시스템에 존재하지 않던 "매칭 종료" 트리거를 만드는 스케줄러.
 * meetTime이 지났는데도 RECRUITING/FULL 상태로 남아있는 매칭을 찾아
 * completeMatching()을 호출해 COMPLETED로 전환 + MatchingCompletedEvent 발행.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingCompletionScheduler {
    private final MatchingRepository matchingRepository;
    private final MatchingService matchingService;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void completeExpiredMatchings() {
        List<Long> targetIds = matchingRepository.findIdsToComplete(
                LocalDateTime.now(),
                List.of(MatchingStatus.RECRUITING, MatchingStatus.FULL)
        );
        if (targetIds.isEmpty()) return;

        log.info("매칭 완료 처리 대상 {}건", targetIds.size());

        for (Long matchingId : targetIds) {
            try {
                matchingService.completeMatching(matchingId);
            } catch (Exception e) {
                log.error("매칭 완료 처리 실패 - matchingId: {}", matchingId, e);
            }
        }
    }
}