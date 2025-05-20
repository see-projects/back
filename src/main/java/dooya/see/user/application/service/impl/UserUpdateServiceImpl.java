package dooya.see.user.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.application.dto.PasswordUpdateCommand;
import dooya.see.user.application.dto.PasswordUpdateResult;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserUpdateCommand;
import dooya.see.user.application.service.UserUpdateService;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.user.application.dto.UserApplicationMapper.*;

@Service
@RequiredArgsConstructor
public class UserUpdateServiceImpl implements UserUpdateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResult updateNickName(String email, UserUpdateCommand command) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateNickName(command.nickName());

        return toResult(user);
    }

    @Transactional
    @Override
    public PasswordUpdateResult updatePassword(String email, PasswordUpdateCommand command) {
        User user = userRepository.findByEmail(email)
                .filter(fountUser -> passwordEncoder.matches(command.currentPassword(), fountUser.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_MATCH_PASSWORD_INFO));
        user.updatePassword(passwordEncoder.encode(command.newPassword()));

        return new PasswordUpdateResult("비밀번호가 성공적으로 변경되었습니다.");
    }
}
