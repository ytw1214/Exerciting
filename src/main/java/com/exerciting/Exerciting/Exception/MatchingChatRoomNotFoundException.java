package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MatchingChatRoomNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public MatchingChatRoomNotFoundException() {
        super(ErrorCode.MATCHING_CHAT_ROOM_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.MATCHING_CHAT_ROOM_NOT_FOUND;
    }
}