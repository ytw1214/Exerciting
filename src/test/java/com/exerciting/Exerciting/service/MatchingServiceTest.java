package com.exerciting.Exerciting.service;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;
    @Autowired
    private UserRepository memberRepository;
    @Autowired
    private MatchingRepository matchingRepository;

    private User host;
    private User guest;

    @BeforeEach
    void setUp() {
        host = memberRepository.save(new User("방장", "host@test.com"));
        guest = memberRepository.save(new User("참가자", "guest@test.com"));
    }

    @Test
    @DisplayName("매칭 생성 성공 테스트")
    void createMatching_Success() {
        // Given
        MatchingRequestDto dto = new MatchingRequestDto("축구 한판", 10, "서울 공원");

        // When
        Long matchingId = matchingService.createMatching(host.getId(), dto);

        // Then
        Matching savedMatching = matchingRepository.findById(matchingId).orElseThrow();
        assertThat(savedMatching.getTitle()).isEqualTo("축구 한판");
        assertThat(savedMatching.getUser().getId()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("매칭 참가 성공 - 인원이 남아있을 때")
    void joinMatching_Success() {
        // Given: 정원 5명인 매칭 생성
        Long matchingId = matchingService.createMatching(host.getId(), new MatchingRequestDto("풋살", 5, "강남"));

        // When
        matchingService.joinMatching(matchingId, guest.getId());

        // Then
        Matching matching = matchingRepository.findById(matchingId).orElseThrow();
        assertThat(matching.getParticipants()).hasSize(2); // 방장 + 게스트
    }

    @Test
    @DisplayName("매칭 참가 실패 - 정원이 초과되었을 때 (Red Flag!)")
    void joinMatching_Fail_Full() {
        // Given: 정원 2명인 매칭 (방장 포함 이미 다 찬 상태라고 가정)
        Long matchingId = matchingService.createMatching(host.getId(), new MatchingRequest("탁구", 2, "체육관"));
        User anotherGuest = memberRepository.save(new User("제3자", "3rd@test.com"));
        matchingService.joinMatching(matchingId, guest.getId()); // 여기서 이미 풀(Full)

        // When & Then
        assertThatThrownBy(() -> matchingService.joinMatching(matchingId, anotherGuest.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("정원이 초과되었습니다");
    }
}