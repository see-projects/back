package dooya.see.user.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dooya.see.common.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

    @InjectMocks
    private UserQueryServiceImpl userQueryService;

    @Mock
    private UserValidator userValidator;

    private final User testUser = testUser();

    @DisplayName("토큰으로 유저 조회 성공 단위테스트")
    @Test
    void user_getUserFromToken_success() {
        // Arrange
        given(userValidator.getUserFromToken(testUser.getEmail())).willReturn(testUser);

        // Act
        User user = userQueryService.getUserFromToken(testUser.getEmail());

        // Assert
        assertAll(
                () -> assertThat(user.getId()).isEqualTo(testUser.getId()),
                () -> assertThat(user.getEmail()).isEqualTo(testUser.getEmail()),
                () -> assertThat(user.getName()).isEqualTo(testUser.getName()),
                () -> assertThat(user.getBirthDate()).isEqualTo(testUser.getBirthDate()),
                () -> assertThat(user.getPhoneNumber()).isEqualTo(testUser.getPhoneNumber()),
                () -> assertThat(user.getRole()).isEqualTo(testUser.getRole())
        );
    }

    @DisplayName("토큰으로 유저 조회 실패 단위테스트 - 존재하지 않는 이메일")
    @Test
    void user_getUserFromToken_failNotEmail() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(userValidator).getUserFromToken(testUser.getEmail());

        // Act && Assert
        assertThatCode(() -> userQueryService.getUserFromToken(testUser.getEmail()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(userValidator).should(times(1)).getUserFromToken(testUser.getEmail());
    }
}
