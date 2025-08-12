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
import dooya.see.member.domain.Member;
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
    private Member testMember;

    @BeforeEach
    void setUp() {
        command = AuthFixture.command();
        testMember = UserFixture.testUser();
    }

    @DisplayName("유저 로그인 성공 단위테스트")
    @Test
    void user_Login_Success() {
        // Arrange
        given(authValidator.validateEmailAndPassword(command.email(), command.password())).willReturn(testMember);
        given(jwtUtil.createAccessToken(testMember.getId(), testMember.getEmail(), testMember.getRole())).willReturn("mocked-jwt-token");

        // Act
        LoginResult result = authService.login(command);

        // Assert
        assertThat(result.email()).isEqualTo(testMember.getEmail());
        assertThat(result.accessToken()).isEqualTo("mocked-jwt-token");

        then(authValidator).should(times(1)).validateEmailAndPassword(command.email(), command.password());
        then(jwtUtil).should(times(1)).createAccessToken(testMember.getId(), testMember.getEmail(), testMember.getRole());
    }

    @DisplayName("유저 로그인 실패 단위테스트")
    @Test
    void user_Login_FailNotEmail() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_MATCH_LOGIN_INFO)).when(authValidator).validateEmailAndPassword(command.email(), command.password());

        // Act && Assert
        assertThatCode(() -> authService.login(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("로그인 정보가 일치하지 않습니다.");

        then(authValidator).should(times(1)).validateEmailAndPassword(command.email(), command.password());
    }
}
