package dooya.see.auth.application.service;

import dooya.see.user.domain.User;

/**
 * {@code AuthValidator} 인터페이스는
 * 인증 관련 입력값 검증 로직을 정의하는 애플리케이션 계층의 계약입니다.
 *
 * <p>주로 이메일과 비밀번호를 검증하여, 올바른 사용자 정보를 반환하거나
 * 검증 실패 시 예외를 발생시키는 책임을 가집니다.
 *
 * <p>구현체는 실제 검증 로직을 구현해야 합니다.
 *
 * @author dooya
 */
public interface AuthValidator {
    /**
     * 이메일과 비밀번호를 검증하고, 유효한 경우 해당 {@link User}를 반환합니다.
     *
     * @param email 검증할 사용자 이메일
     * @param password 검증할 사용자 비밀번호
     * @return 검증된 사용자 {@link User} 객체
     */
    User validateEmailAndPassword(String email, String password);
}
