package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.application.UserUpdateCommand;
import dooya.see.user.domain.User;

public class UserDtoMapper {

    public static UserSignUpCommand toSignCommand(UserSignUpRequest request) {
        return new UserSignUpCommand(
                request.email(),
                request.name(),
                request.password(),
                request.nickName());
    }

    public static UserSignUpResponse toSignResponse(User user) {
        return new UserSignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getRole()
        );
    }

    public static UserUpdateCommand toUpdateCommand(UserUpdateRequest request) {
        return new UserUpdateCommand(request.nickName());
    }

    public static UserUpdateResponse toUpdateResponse(User user) {
        return new UserUpdateResponse(user.getNickName());
    }
}
