package com.exerciting.Exerciting.Domain.user.dto.request;

/** 가입과 비밀번호 변경이 같은 규칙을 쓰도록 한 곳에 둔다. */
public final class PasswordPolicy {
    public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$";
    public static final String MESSAGE = "패스워드는 영문자, 숫자, 특수기호를 포함하여 8자 이상이어야합니다.";

    private PasswordPolicy() {
    }
}
