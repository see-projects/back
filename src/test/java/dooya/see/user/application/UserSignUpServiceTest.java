package dooya.see.user.application;

import dooya.see.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
public class UserSignUpServiceTest {

    @InjectMocks
    private UserSignUpServiceImpl userSignUpService;

    @DisplayName("유저 회원가입 성공 단위테스트")
    @Test
    void user_signUp_success() {
        UserSignUpRequest request = UserSignUpRequest.builder()
                .email("test@see.com")
                .name("testName")
                .password("testPassword")
                .birthDate(LocalDate.of(2001, 1, 4))
                .phoneNumber("01012345678")
                .build();

        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

        UserSignUpResponse response = userSignUpService.userSignUp(request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.email()).isEqualTo(request.email()),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.birthDate()).isEqualTo(request.birthDate()),
                () -> assertThat(response.phoneNumber()).isEqualTo(request.phoneNumber()),
                () -> assertThat(response.role()).isEqualTo(Role.USER)
        );
    }
}
