package dooya.see.user.application;

import dooya.see.user.domain.User;

public interface UserSignUpService {
    User userSignUp(UserSignUpCommand command);
}
