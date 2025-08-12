package dooya.see.domain.auth;

import dooya.see.domain.member.Member;

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
