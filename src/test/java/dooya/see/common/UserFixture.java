package dooya.see.common;

import dooya.see.user.application.dto.*;
import dooya.see.user.presentation.dto.PasswordUpdateRequest;
import dooya.see.user.presentation.dto.UserSignUpRequest;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.presentation.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static UserSignUpRequest signUpRequest() {
        return new UserSignUpRequest("dooya@see.com", "testName", "testPassword", "testNickName");
    }

    public static UserUpdateRequest nickNameUpdateRequest() {
        return new UserUpdateRequest("updateNickName");
    }

    public static PasswordUpdateRequest passwordUpdateRequest() {
        return new PasswordUpdateRequest("testPassword", "updatePassword");
    }

    public static PasswordUpdateRequest passwordUpdateRequestFail() {
        return new PasswordUpdateRequest("failPassword", "updatePassword");
    }

    public static UserSignUpCommand signUpCommand() {
        return new UserSignUpCommand("test@see.com", "testName", "testPassword", "testNickName");
    }

    public static UserUpdateCommand updateCommand() {
        return new UserUpdateCommand("updateNickName");
    }

    public static PasswordUpdateCommand passwordUpdateCommand() {
        return new PasswordUpdateCommand("testPassword", "updatePassword");
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

    public static UserResult testUserResult() {
        return new UserResult(1L, "test@see.com", "testName", "testNickName", Role.of("USER"));
    }

    public static PasswordUpdateResult passwordUpdateResult() {
        return new PasswordUpdateResult("비밀번호가 성공적으로 변경되었습니다.");
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
