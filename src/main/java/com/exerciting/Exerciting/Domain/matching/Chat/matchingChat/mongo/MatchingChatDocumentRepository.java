package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchingChatDocumentRepository extends MongoRepository<MatchingChatDocument, String> {

    // MySQL의 findByMatchingChatRoomOrderBySendAtDesc와 같은 조건 (최신순 + Slice)
    Slice<MatchingChatDocument> findByRoomIdOrderBySendAtDesc(Long roomId, Pageable pageable);
}
