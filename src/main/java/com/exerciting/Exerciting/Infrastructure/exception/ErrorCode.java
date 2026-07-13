package com.exerciting.Exerciting.Infrastructure.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 유저를 찾을 수 없습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED,"권한이 없습니다."),

    //Matching
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 매칭을 찾을 수 없습니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST,"유효하지 않은 시간입니다."),
    DISMATCHED_SIZE(HttpStatus.BAD_REQUEST,"인원 수가 맞지 않습니다."),

    //MatchingChatRoom
    MATCHING_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 채팅방을 찾을 수 없습니다."),

    INVALID_INPUT(HttpStatus.BAD_REQUEST,"잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서버 오류가 발생했습니다."),

    CRAWLING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "크롤링 중 오류가 발생했습니다."),
    UNSUPPORTEDYEAR_ERROR(HttpStatus.BAD_REQUEST,"지원하지 않는 연도입니다. 2000년부터 현재 연도까지 조회 가능합니다.");

    private final HttpStatus status;
    private final String message;
}
