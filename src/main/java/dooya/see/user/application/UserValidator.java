package dooya.see.user.application;

import dooya.see.user.domain.User;

public interface UserValidator {
    void validateDuplicateEmail(String email);
    User getUserFromToken(String email);
}
