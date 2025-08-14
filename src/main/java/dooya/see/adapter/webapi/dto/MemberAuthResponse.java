package dooya.see.adapter.webapi.dto;

import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;

public record MemberAuthResponse(
        Long memberId,
        String email,
        String nickname,
        String accessToken
) {
    public static MemberAuthResponse from(LoginResult loginResult) {
        Member member = loginResult.member();

        return new MemberAuthResponse(
                member.getId(),
                member.getEmail().address(),
                member.getNickname(),
                loginResult.accessToken()
        );
    }
}
