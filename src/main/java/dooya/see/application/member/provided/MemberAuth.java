package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.LoginResult;

/**
 * 회원 인증 Primary Port
 */
public interface MemberAuth {

    /**
     * 회원 로그인을 수행합니다.
     */
    LoginResult login(MemberAuthRequest memberAuthRequest);

    /**
     * 액세스 토큰을 통해 현재 로그인된 회원 정보를 조회합니다.
     */
    Member getCurrentMember(String token);
}