package dooya.see.auth.application.dto;

import lombok.Builder;

/**
 * {@code LoginCommand}는 로그인 요청 시
 * 클라이언트로부터 전달받는 인증 정보를 담는 데이터 전송 객체(DTO)입니다.
 *
 * <p>주로 이메일과 비밀번호를 포함하며, 인증 과정에서 사용됩니다.
 *
 * <p>{@link lombok.Builder}를 사용하여 빌더 패턴으로 객체를 생성할 수 있습니다.
 *
 * @author dooya
 */
@Builder
public record LoginCommand(
        String email,
        String password
) {
}
