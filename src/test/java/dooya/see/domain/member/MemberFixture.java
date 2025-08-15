package dooya.see.domain.member;

public class MemberFixture {
    public static MemberRegisterRequest createMemberRegisterRequest(String email) {
        return new MemberRegisterRequest(email, "dooya", "longSecret");
    }

    public static MemberRegisterRequest createMemberRegisterRequest() {
        return createMemberRegisterRequest("dooya@see.com");
    }

    public static MemberInfoUpdateRequest createMemberInfoUpdateRequest() {
        return new MemberInfoUpdateRequest("dooyaya", "niceaddress", "자기소개");
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

    public static MemberAuthRequest createMemberAuthRequest() {
        return createMemberAuthRequest("dooya@see.com");
    }

    public static MemberAuthRequest createMemberAuthRequest(String email) {
        return createMemberAuthRequest(email, "longSecret");
    }

    public static MemberAuthRequest createMemberAuthRequest(String email, String password) {
        return new MemberAuthRequest(email, password);
    }

    public static MemberAuthRequest createAuthRequestWithPassword(String password) {
        return createMemberAuthRequest("dooya@see.com", password);
    }
}
