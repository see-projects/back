package dooya.see.auth.presentation.dto;

import lombok.Builder;

/**
 * {@code LoginResponse} 레코드는
 * 로그인 성공 후 클라이언트에게 반환되는 응답 데이터를 담는 DTO입니다.
 *
 * <p>JWT 액세스 토큰(accessToken)과 함께
 * 사용자 식별자(id), 이메일(email), 이름(name), 닉네임(nickName) 정보를 포함합니다.
 *
 * <p>인증 완료 후, 클라이언트가 사용자 정보를 확인하거나
 * 인증 상태를 유지하는 데 사용됩니다.
 *
 * @author dooya
 */
@Builder
public record LoginResponse(
        String accessToken,
        Long id,
        String email,
        String name,
        String nickName
) {
}
