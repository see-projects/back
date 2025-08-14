package dooya.see.domain.member;

public class MemberFixture {
    public static MemberRegisterRequest createMemberRegisterRequest(String email) {
        return new MemberRegisterRequest(email, "dooya", "longSecret");
    }

    public static MemberRegisterRequest createMemberRegisterRequest() {
        return createMemberRegisterRequest("dooya@see.com");
    }

    public static PasswordEncoder createPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(String password) {
                return password.toUpperCase();
            }

            @Override
            public boolean matches(String password, String passwordHash) {
                return encode(password).equals(passwordHash);
            }
        };
    }

    public static LoginRequest createLoginRequest() {
        return createLoginRequest("dooya@see.com");
    }

    public static LoginRequest createLoginRequest(String email) {
        return createLoginRequest(email, "longSecret");
    }

    public static LoginRequest createLoginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    public static LoginRequest createLoginRequestWithPassword(String password) {
        return createLoginRequest("dooya@see.com", password);
    }
}
