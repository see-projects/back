package dooya.see.user.application;

import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignUpServiceImpl implements UserSignUpService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResult userSignUp(UserSignUpCommand command) {
        userValidator.validateDuplicateEmail(command.email());
        User user = UserApplicationMapper.toEntity(command, passwordEncoder);
        userRepository.save(user);

        return UserApplicationMapper.toResult(user);
    }
}
