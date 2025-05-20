package dooya.see.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    USER_NOT_MATCH_PASSWORD_INFO(HttpStatus.BAD_REQUEST, "비밀번호 정보가 일치하지 않습니다."),

    // Login Error
    USER_NOT_MATCH_LOGIN_INFO(HttpStatus.BAD_REQUEST, "로그인 정보가 일치하지 않습니다."),

    // Role Error
    ROLE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 권한입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Validation Error
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),

    // JWT Error
    WRONG_TYPE_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNKNOWN_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "토큰 값이 존재하지 않습니다."),
    MISSING_TOKEN(HttpStatus.BAD_REQUEST, "토큰이 누락되었습니다.");

    private final HttpStatus status;
    private final String message;
}