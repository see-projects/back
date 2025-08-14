package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.LoginResult;

public interface MemberAuth {
    LoginResult login(MemberAuthRequest memberAuthRequest);

    Member getCurrentMember(String token);
}
