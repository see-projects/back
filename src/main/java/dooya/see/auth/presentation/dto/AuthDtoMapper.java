package dooya.see.auth.presentation.dto;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.user.domain.User;

/**
 * {@code AuthDtoMapper} 클래스는
 * 인증 관련 데이터 전송 객체(DTO) 변환을 담당하는 매퍼 클래스입니다.
 *
 * <p>클라이언트로부터 받은 {@link LoginRequest}를 도메인 서비스가 사용하는
 * {@link LoginCommand}로 변환하며,
 *
 * <p>서비스 계층에서 반환된 {@link LoginResult}를 클라이언트에 반환할
 * {@link LoginResponse}로 변환하는 역할을 합니다.
 *
 * <p>이 클래스를 통해 계층 간 데이터 변환을 명확히 분리하여
 * 코드의 응집도와 유지보수성을 높입니다.
 *
 * @author dooya
 */
public class AuthDtoMapper {

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                request.email(),
                request.password()
        );
    }

    public static LoginResponse toResponse(LoginResult result) {
        User user = result.user();

        return new LoginResponse(
                result.accessToken(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName()
        );
    }
}
