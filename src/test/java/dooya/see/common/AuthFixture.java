package dooya.see.common;

import dooya.see.auth.application.LoginRequest;

public class AuthFixture {

    public static LoginRequest request() {
        return new LoginRequest("test@see.com", "testName");
    }
}
