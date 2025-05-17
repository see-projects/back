package dooya.see.user.application;

import dooya.see.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserValidator userValidator;

    @Override
    public User getUserFromToken(String email) {
        return userValidator.getUserFromToken(email);
    }
}
