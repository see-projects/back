package dooya.see.application.member.provided;

import dooya.see.domain.member.LoginRequest;
import dooya.see.domain.member.LoginResult;

public interface MemberAuth {
    LoginResult login(LoginRequest loginRequest);
}
