package dooya.see.auth.application.dto;

import dooya.see.member.domain.Member;

public class AuthApplicationMapper {

    public static LoginResult toResult(Member member, String accessToken) {
        return new LoginResult(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickName(),
                accessToken
        );
    }
}
