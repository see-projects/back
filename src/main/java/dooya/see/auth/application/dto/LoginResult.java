package dooya.see.auth.application.dto;

import dooya.see.user.domain.User;

/**
 * {@code LoginResult}는 로그인 처리 결과를 나타내는 DTO입니다.
 *
 * <p>로그인에 성공한 사용자의 {@link User} 정보와 함께,
 * 인증에 사용되는 액세스 토큰(accessToken)을 포함합니다.
 *
 * <p>정적 팩토리 메서드 {@link #of(User, String)}를 제공하여
 * 객체 생성 시 명확성을 높였습니다.
 *
 * <p>주로 로그인 응답 시 클라이언트에게 전달되는 데이터를 캡슐화합니다.
 *
 * @author dooya
 */
public record LoginResult(
        User user,
        String accessToken
) {
    public static LoginResult of(User user, String accessToken) {
        return new LoginResult(user, accessToken);
    }
}
