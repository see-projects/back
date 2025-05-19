package dooya.see.user.application;

import dooya.see.user.domain.User;

public interface UserUpdateService {
    User updateNickName(User user, UserUpdateCommand command);
}
