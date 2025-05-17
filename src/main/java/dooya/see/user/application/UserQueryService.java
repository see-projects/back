package dooya.see.user.application;

import dooya.see.user.domain.User;

public interface UserQueryService {
    User getUserFromToken(String email);
}
