package dooya.see.user.application;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserSignUpServiceTest {

    @InjectMocks
    private UserSignUpServiceImpl userSignUpService;

    @Mock
    private UserRepository userRepository;

    @DisplayName("유저 회원가입 성공 단위테스트")
    @Test
    void user_signUp_success() {
        // Arrange
        UserSignUpRequest request = new UserSignUpRequest("test@see.com", "testName", "testPassword", LocalDate.of(2001, 1, 4), "01012345678");

        given(userRepository.findByEmail(request.email())).willReturn(java.util.Optional.empty());

        User testUser = User.signUpUser(request.email(), request.name(), request.password(), request.birthDate(), request.phoneNumber(), Role.of("USER"));
        ReflectionTestUtils.setField(testUser, "id", 1L);
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

        then(userRepository).should(times(1)).findByEmail(request.email());
        then(userRepository).should(times(1)).save(any(User.class));
    }
}
