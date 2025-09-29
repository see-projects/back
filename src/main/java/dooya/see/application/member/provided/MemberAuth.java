package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.dto.MemberAuthRequest;
import dooya.see.domain.member.LoginResult;

/**
 * 회원 인증 기능을 제공하는 인터페이스
 */
public interface MemberAuth {

    /**
     * 회원의 자격 증명을 통해 로그인 작업을 수행합니다.
     *
     * @param memberAuthRequest 회원 인증 요청 객체 (이메일과 비밀번호 포함)
     * @return 로그인 결과 객체로, 회원 정보와 액세스 토큰을 포함합니다.
     * @throws dooya.see.domain.member.exception.AuthenticateException 자격 증명이 유효하지 않거나 계정이 비활성화된 경우
     */
    LoginResult login(MemberAuthRequest memberAuthRequest);

    /**
     * 주어진 토큰을 사용하여 현재 인증된 회원 정보를 반환합니다.
     *
     * @param token 인증에 사용되는 액세스 토큰
     * @return 현재 인증된 회원 객체
     * @throws dooya.see.domain.member.exception.AuthenticateException 유효하지 않은 토큰이거나 인증 실패 시
     */
    Member getCurrentMember(String token);
}