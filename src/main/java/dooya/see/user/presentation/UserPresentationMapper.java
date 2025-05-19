package dooya.see.user.presentation;

import dooya.see.user.application.UserResult;
import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.application.UserUpdateCommand;

public class UserPresentationMapper {

    public static UserSignUpCommand toSignCommand(UserSignUpRequest request) {
        return new UserSignUpCommand(
                request.email(),
                request.name(),
                request.password(),
                request.nickName());
    }

    public static UserSignUpResponse toSignResponse(UserResult userResult) {
        return new UserSignUpResponse(
                userResult.id(),
                userResult.email(),
                userResult.name(),
                userResult.nickName(),
                userResult.role()
        );
    }

    public static UserUpdateCommand toUpdateCommand(UserUpdateRequest request) {
        return new UserUpdateCommand(request.nickName());
    }

    public static UserUpdateResponse toUpdateResponse(UserResult userResult) {
        return new UserUpdateResponse(userResult.nickName());
    }
}
