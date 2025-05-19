package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.domain.User;

public class UserDtoMapper {

    public static UserSignUpCommand toCommand(UserSignUpRequest request) {
        return new UserSignUpCommand(
                request.email(),
                request.name(),
                request.password(),
                request.nickName());
    }

    public static UserSignUpResponse toResponse(User user) {
        return new UserSignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getRole()
        );
    }
}
