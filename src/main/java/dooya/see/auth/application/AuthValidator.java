package dooya.see.auth.application;

import dooya.see.user.domain.User;

public interface AuthValidator {
    User validateEmailAndPassword(String email, String password);
}
