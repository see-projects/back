package dooya.see.user.application;

public interface UserUpdateService {
    UserResult updateNickName(String email, UserUpdateCommand command);
}
