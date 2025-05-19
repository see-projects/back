package dooya.see.user.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserSignUpCommand;
import dooya.see.user.application.service.impl.UserSignUpServiceImpl;
import dooya.see.user.application.service.UserValidator;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import dooya.see.common.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSignUpServiceTest {

    @InjectMocks
    private UserSignUpServiceImpl userSignUpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @DisplayName("유저 회원가입 성공 단위테스트")
    @Test
    void user_signUp_success() {
        // Arrange
        UserSignUpCommand command = UserFixture.signUpCommand();
        User testUser = UserFixture.createTestUser(command);

        doNothing().when(userValidator).validateDuplicateEmail(command.email());
        given(passwordEncoder.encode(command.password())).willReturn(testUser.getPassword());
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // Act
        UserResult result = userSignUpService.userSignUp(command);

        // Assert
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.email()).isEqualTo(command.email()),
                () -> assertThat(result.name()).isEqualTo(command.name()),
                () -> assertThat(result.nickName()).isEqualTo(command.nickName()),
                () -> assertThat(result.role()).isEqualTo(Role.USER)
        );

        then(userValidator).should(times(1)).validateDuplicateEmail(command.email());
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @DisplayName("유저 회원가입 실패 단위테스트 - 중복되는 이메일")
    @Test
    void user_signUp_fail() {
        // Arrange
        UserSignUpCommand command = UserFixture.signUpCommand();
        doThrow(new CustomException(ErrorCode.USER_ALREADY_EXISTS)).when(userValidator).validateDuplicateEmail(command.email());

        // Act && Assert
        assertThatCode(() -> userSignUpService.userSignUp(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 존재하는 사용자입니다.");

        then(userValidator).should(times(1)).validateDuplicateEmail(command.email());
        then(userRepository).shouldHaveNoInteractions();
    }
}
