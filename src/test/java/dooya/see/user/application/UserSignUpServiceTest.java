package dooya.see.user.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import dooya.see.user.fixture.UserSignUpFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        UserSignUpRequest request = UserSignUpFixture.request();
        User testUser = UserSignUpFixture.createTestUser(request);

        doNothing().when(userValidator).validateDuplicateEmail(request.email());
        given(passwordEncoder.encode(request.password())).willReturn(testUser.getPassword());
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // Act
        UserSignUpResponse response = userSignUpService.userSignUp(request);

        // Assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.email()).isEqualTo(request.email()),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.birthDate()).isEqualTo(request.birthDate()),
                () -> assertThat(response.phoneNumber()).isEqualTo(request.phoneNumber()),
                () -> assertThat(response.role()).isEqualTo(Role.USER)
        );

        then(userValidator).should(times(1)).validateDuplicateEmail(request.email());
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @DisplayName("유저 회원가입 실패 단위테스트 - 중복되는 이메일")
    @Test
    void user_signUp_fail() {
        // Arrange
        UserSignUpRequest request = UserSignUpFixture.request();
        doThrow(new CustomException(ErrorCode.USER_ALREADY_EXISTS)).when(userValidator).validateDuplicateEmail(request.email());

        // Act && Assert
        assertThrows(CustomException.class, () -> userSignUpService.userSignUp(request));

        then(userValidator).should(times(1)).validateDuplicateEmail(request.email());
        then(userRepository).shouldHaveNoInteractions();
    }
}
