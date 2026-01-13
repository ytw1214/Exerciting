package com.exerciting.Exerciting.Exception;

public class UserNotFoundException extends RuntimeException {

    // 이 생성자가 있어야 네 Service에서 메시지를 전달할 수 있어.
    public UserNotFoundException(String message) {
        super(message);
    }
}