package dooya.see.user.application;

import lombok.Builder;

@Builder
public record UserSignUpCommand(
        String email,
        String name,
        String password,
        String nickName
) {
}
