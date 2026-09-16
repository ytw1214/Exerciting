package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class MatchingChatRoomNotFoundException extends BusinessException {

    public MatchingChatRoomNotFoundException() {
        super(ErrorCode.MATCHING_CHAT_ROOM_NOT_FOUND);
    }
}