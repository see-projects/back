package dooya.see.common;

import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.presentation.UserSignUpRequest;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class UserFixture {

    public static UserSignUpRequest request() {
        return new UserSignUpRequest("test@see.com", "testName", "testPassword", "testNickName");
    }

    public static UserSignUpCommand command() {
        return new UserSignUpCommand("test@see.com", "testName", "testPassword", "nickName");
    }

    public static User createTestUser(UserSignUpCommand command) {
        User testUser = User.signUpUser(
                command.email(),
                command.name(),
                command.password(),
                command.nickName(),
                Role.of("USER")
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);
        return testUser;
    }

    public static User testUser() {
        return User.builder()
                .email("test@see.com")
                .name("testName")
                .password("$2a$10$DOWSDdkg2YqXWB3S1.CzHeeI6qHtDc0lYBFfN4y3pFehUcbDoztYm")
                .nickName("testNickName")
                .role(Role.of("USER"))
                .build();
    }

    public static User mockUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .email("test@see.com")
                .name("testName")
                .password(passwordEncoder.encode("testPassword"))
                .nickName("testNickName")
                .role(Role.of("USER"))
                .build();
    }
}
