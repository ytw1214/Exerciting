package com.exerciting.Exerciting.Domain.matching.matchingParticipant.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.MatchingNotFoundException;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingParticipantService {
    private final MatchingParticipantRepository matchingParticipantRepository;
    private final MatchingRepository matchingRepository;
    private final UserRepository userRepository;
    private final MatchingChatRoomRepository matchingChatRoomRepository;
    public List<MatchingParticipant> getUserByMatching(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new InvalidInputException("잘못된 매칭입니다."));
        return matchingParticipantRepository.findByMatchingId(matching.getId());
    }

    @Transactional
    public void joinMatching(Long matchingId, Long userId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException("해당 매칭이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저가 존재하지 않습니다."));

        if(matchingParticipantRepository.existsByMatchingAndUser(matching,user)) {
            throw new InvalidInputException("이미 참가한 매칭입니다.");
        }
        long currentParticipant = matchingParticipantRepository.countByMatching(matching);
        if(currentParticipant >= matching.getMaxPerson()) {
            throw new InvalidInputException("정원이 초과되었습니다.");
        }
        matchingParticipantRepository.save(new MatchingParticipant(user,matching,LocalDateTime.now()));

        boolean chatRoomExists = matchingChatRoomRepository.findByMatching(matching).isPresent();
        if (!chatRoomExists) {
            matchingChatRoomRepository.save(
                    MatchingChatRoom.builder()
                            .matching(matching)
                            .requester(user)
                            .build()
            );
        }
    }

}
