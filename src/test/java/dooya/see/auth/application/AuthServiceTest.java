package dooya.see.auth.application;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.application.service.impl.AuthServiceImpl;
import dooya.see.auth.application.service.AuthValidator;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.AuthFixture;
import dooya.see.common.UserFixture;
import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private JwtUtil jwtUtil;

    private LoginCommand command;
    private User testUser;

    @BeforeEach
    void setUp() {
        command = AuthFixture.command();
        testUser = UserFixture.testUser();
    }

    @DisplayName("유저 로그인 성공 단위테스트")
    @Test
    void user_login_success() {
        // Arrange
        given(authValidator.validateEmailAndPassword(command.email(), command.password())).willReturn(testUser);
        given(jwtUtil.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole())).willReturn("mocked-jwt-token");

        // Act
        LoginResult result = authService.login(command);

        // Assert
        assertThat(result.user().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.accessToken()).isEqualTo("mocked-jwt-token");

        then(authValidator).should(times(1)).validateEmailAndPassword(command.email(), command.password());
        then(jwtUtil).should(times(1)).createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
    }

    @DisplayName("유저 로그인 실패 단위테스트")
    @Test
    void user_login_failNotEmail() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_MATCH_LOGIN_INFO)).when(authValidator).validateEmailAndPassword(command.email(), command.password());

        // Act && Assert
        assertThatCode(() -> authService.login(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("로그인 정보가 일치하지 않습니다.");

        then(authValidator).should(times(1)).validateEmailAndPassword(command.email(), command.password());
    }
}
