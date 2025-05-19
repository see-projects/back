package dooya.see.user.application;

import dooya.see.common.UserFixture;
import dooya.see.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceTest {

    @InjectMocks
    private UserUpdateServiceImpl userUpdateService;

    @DisplayName("유저 닉네임 업데이트 성공 테스트")
    @Test
    void user_nickNameUpdate_success() {
        // Arrange
        User testUser = UserFixture.testUser();
        UserUpdateCommand command = UserFixture.updateCommand();

        // Act
        userUpdateService.updateNickName(testUser, command);

        // Assert
        assertThat(testUser.getNickName()).isEqualTo("updateNickName");
    }
}
