package com.exerciting.Exerciting.Infrastructure.exception;

import com.exerciting.Exerciting.Exception.BusinessException;
import com.exerciting.Exerciting.Exception.CrawlingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 응답 형식 통일 + 로그 레벨 분리.
 *
 * ResponseEntityExceptionHandler를 상속하는 이유:
 * 예전에는 @ExceptionHandler(Exception.class)가 Spring MVC의 표준 예외(잘못된 JSON, 파라미터 타입 불일치,
 * 필수 파라미터 누락, 허용되지 않은 메서드, 없는 경로)까지 받아 전부 500 + ERROR 스택트레이스로 바꿨다.
 * 이제 그런 예외는 원래 상태 코드(400/404/405 등)와 WARN 한 줄로 나가고,
 * 정말 예상하지 못한 예외만 500 + ERROR로 남는다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(CrawlingException.class)
    public ResponseEntity<ErrorResponse> handleCrawlingException(CrawlingException e) {
        log.error("외부 데이터 수집 실패 - 소스 구조 변경 또는 응답 불가 의심", e);
        return ResponseEntity
                .status(ErrorCode.CRAWLING_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.CRAWLING_ERROR));
    }

    // 부모 클래스의 handleException(Exception, WebRequest)과 이름이 겹치지 않게 한다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /** @Valid 검증 실패: 첫 번째 필드 오류를 메시지로 돌려준다. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.warn("ValidationException: {}", detail);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, detail));
    }

    /** 그 밖의 Spring MVC 표준 예외: 상태 코드는 Spring이 정한 값을 유지하고 본문 형식만 맞춘다. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        } else {
            log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(ErrorResponse.of(errorCodeOf(statusCode)));
    }

    private ErrorCode errorCodeOf(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            return ErrorCode.RESOURCE_NOT_FOUND;
        }
        if (statusCode.value() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            return ErrorCode.METHOD_NOT_ALLOWED;
        }
        return statusCode.is4xxClientError() ? ErrorCode.INVALID_INPUT : ErrorCode.INTERNAL_SERVER_ERROR;
    }
}
