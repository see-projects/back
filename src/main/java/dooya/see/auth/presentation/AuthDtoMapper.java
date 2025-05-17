package dooya.see.auth.presentation;

import dooya.see.auth.application.LoginCommand;
import dooya.see.auth.application.LoginResult;
import dooya.see.user.domain.User;

public class AuthDtoMapper {

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                request.email(),
                request.password()
        );
    }

    public static LoginResponse toResponse(LoginResult result) {
        User user = result.user();

        return new LoginResponse(
                result.accessToken(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBirthDate(),
                user.getPhoneNumber()
        );
    }
}
