package com.exerciting.Exerciting.Domain.matching.matchingParticipant.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.user.service.UserService;
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
    private final MatchingService matchingService;
    private final UserService userService;
    public List<MatchingParticipant> getUserByMatching(Long matchingId) {
        Matching matching = matchingService.findById(matchingId);
        return matchingParticipantRepository.findByMatchingId(matching.getId());
    }

    @Transactional
    public void joinMatching(Long matchingId, Long userId) {
        Matching matching = matchingService.findById(matchingId);
        User user = userService.getUserById(userId);

        if(matchingParticipantRepository.existsByMatchingAndUser(matching,user)) {
            throw new InvalidInputException();
        }
        long currentParticipant = matchingParticipantRepository.countByMatching(matching);
        if(currentParticipant >= matching.getMaxPerson()) {
            throw new InvalidInputException();
        }
        matchingParticipantRepository.save(new MatchingParticipant(user,matching,LocalDateTime.now()));
    }
    public void updateLastReadAt(Matching matching, User user){
        MatchingParticipant participant = matchingParticipantRepository.findByMatchingAndUser(matching,user)
                .orElseThrow(()-> new InvalidInputException());
        participant.updateLastReadAt(LocalDateTime.now());
    }
}
