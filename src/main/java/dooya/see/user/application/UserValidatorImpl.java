package dooya.see.user.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;

    @Override
    public void validateDuplicateEmail(String email) {
        userRepository.findByEmail(email)
                .ifPresent(UserValidatorImpl::throwUserAlreadyExists);
    }

    private static void throwUserAlreadyExists(User user) {
        throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
    }
}
