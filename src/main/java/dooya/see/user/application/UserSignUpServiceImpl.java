package dooya.see.user.application;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignUpServiceImpl implements UserSignUpService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserSignUpResponse userSignUp(UserSignUpRequest userSignUpRequest) {
        userRepository.findByEmail(userSignUpRequest.email());

        User user = userSaveAndGet(userSignUpRequest);

        return UserSignUpResponse.from(user);
    }

    private User userSaveAndGet(UserSignUpRequest userSignUpRequest) {
        return userRepository.save(User.singUpUser(userSignUpRequest.email(), userSignUpRequest.name(), userSignUpRequest.password(), userSignUpRequest.birthDate(), userSignUpRequest.phoneNumber(), Role.of("USER")));
    }
}
