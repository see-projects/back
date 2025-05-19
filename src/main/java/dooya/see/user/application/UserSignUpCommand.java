package dooya.see.user.application;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserSignUpCommand(
        String email,
        String name,
        String password,
        String nickName
) {
}
