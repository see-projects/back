package dooya.see.application.member.required;

import dooya.see.domain.member.Member;

/**
 * 토큰을 관리하는 인터페이스
 */
public interface TokenManager {
    /**
     * 주어진 회원 정보를 기반으로 인증 토큰을 생성합니다.
     *
     * @param member 토큰을 생성하기 위한 회원 정보
     * @return 생성된 인증 토큰
     */
    String generateToken(Member member);

    /**
     * JWT 토큰에서 이메일 정보를 추출합니다.
     *
     * @param token 이메일 정보를 추출할 JWT 토큰
     * @return 토큰에 포함된 이메일 정보
     * @throws dooya.see.domain.member.exception.AuthenticateException 토큰이 유효하지 않거나 파싱할 수 없는 경우
     */
    String extractEmailFromToken(String token);

    /**
     * JWT 토큰에서 회원 ID를 추출합니다.
     *
     * @param token 회원 ID를 추출할 JWT 토큰
     * @return 토큰에 포함된 회원 ID
     * @throws dooya.see.domain.member.exception.AuthenticateException 토큰이 유효하지 않거나 파싱할 수 없는 경우
     */
    Long extractMemberIdFromToken(String token);
}
