package dooya.see.auth.application.service;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;

/**
 * {@code AuthService} 인터페이스는
 * 인증(로그인) 관련 비즈니스 로직을 추상화한 서비스 계층의 계약을 정의합니다.
 *
 * <p>로그인 요청 정보를 담은 {@link LoginCommand}를 받아
 * 인증 처리 후 로그인 결과를 {@link LoginResult}로 반환하는 메서드를 제공합니다.
 *
 * <p>구현체는 이 인터페이스를 구현하여 실제 로그인 로직(예: 사용자 검증, 토큰 발급 등)을 담당합니다.
 *
 * @author dooya
 */
public interface AuthService {
    /**
     * 로그인 요청을 처리합니다.
     *
     * @param command 로그인에 필요한 정보를 담은 {@link LoginCommand}
     * @return 로그인 성공 시 인증 결과를 담은 {@link LoginResult}
     */
    LoginResult login(LoginCommand command);
}
