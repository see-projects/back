package dooya.see.user.application;

import dooya.see.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUpdateServiceImpl implements UserUpdateService {

    @Override
    public User updateNickName(User user, UserUpdateCommand command) {
        user.updateNickName(command.nickName());
        return user;
    }
}
