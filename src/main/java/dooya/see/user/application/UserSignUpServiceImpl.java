package dooya.see.user.application;

import dooya.see.user.domain.Role;
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
    public UserSignUpResponse userSignUp(UserSignUpRequest request) {
        userValidator.validateDuplicateEmail(request.email());
        User user = userSaveAndGet(request);

        return UserSignUpResponse.from(user);
    }

    private User userSaveAndGet(UserSignUpRequest request) {
        return userRepository.save(User.signUpUser(request.email(), request.name(), passwordEncoder.encode(request.password()), request.birthDate(), request.phoneNumber(), Role.of("USER")));
    }
}
