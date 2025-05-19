package dooya.see.user.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserUpdateCommand;
import dooya.see.user.application.service.UserUpdateService;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.user.application.dto.UserApplicationMapper.*;

@Service
@RequiredArgsConstructor
public class UserUpdateServiceImpl implements UserUpdateService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserResult updateNickName(String email, UserUpdateCommand command) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateNickName(command.nickName());

        return toResult(user);
    }
}
