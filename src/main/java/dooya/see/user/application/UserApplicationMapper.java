package dooya.see.user.application;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserApplicationMapper {

    public static User toEntity(UserSignUpCommand command, PasswordEncoder passwordEncoder) {
        return User.signUpUser(
                command.email(),
                command.name(),
                passwordEncoder.encode(command.password()),
                command.nickName(),
                Role.of("USER")
        );
    }

    public static UserResult toResult(User user) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getRole()
        );
    }
}
