package dooya.see.common;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.presentation.dto.LoginRequest;

public class AuthFixture {

    public static LoginRequest request() {
        return new LoginRequest("test@see.com", "testPassword");
    }

    public static LoginCommand command() {
        return new LoginCommand("test@see.com", "testPassword");
    }

    public static LoginResult result() {
        return new LoginResult(1L, "test@see.com", "testName", "testNickName", "mocked-jwt-token");
    }
}
