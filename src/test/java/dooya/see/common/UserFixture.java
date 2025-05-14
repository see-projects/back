package dooya.see.common;

import dooya.see.user.application.UserSignUpRequest;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class UserFixture {

    public static UserSignUpRequest request() {
        return new UserSignUpRequest("test@see.com", "testName", "testPassword", LocalDate.of(2001, 1, 4), "01012345678");
    }

    public static User createTestUser(UserSignUpRequest request) {
        User testUser = User.signUpUser(
                request.email(),
                request.name(),
                request.password(),
                request.birthDate(),
                request.phoneNumber(),
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
                .birthDate(LocalDate.of(2001, 1, 4))
                .phoneNumber("01012345678")
                .role(Role.of("USER"))
                .build();
    }
}
