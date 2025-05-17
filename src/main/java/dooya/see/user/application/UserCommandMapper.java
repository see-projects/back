package dooya.see.user.application;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserCommandMapper {

    public static User toEntity(UserSignUpCommand command, PasswordEncoder passwordEncoder) {
        return User.signUpUser(
                command.email(),
                command.name(),
                passwordEncoder.encode(command.password()),
                command.birthDate(),
                command.phoneNumber(),
                Role.of("USER")
        );
    }
}
