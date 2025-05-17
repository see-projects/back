package dooya.see.auth.application;

import dooya.see.user.domain.User;

public record LoginResult(
        User user,
        String accessToken
) {
    public static LoginResult of(User user, String accessToken) {
        return new LoginResult(user, accessToken);
    }
}
