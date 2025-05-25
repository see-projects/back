package dooya.see.auth.application.dto;

import dooya.see.post.application.dto.PostResult;
import dooya.see.post.domain.Post;
import dooya.see.user.domain.User;

public class AuthApplicationMapper {

    public static LoginResult toResult(User user, String accessToken) {
        return new LoginResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                accessToken
        );
    }
}
