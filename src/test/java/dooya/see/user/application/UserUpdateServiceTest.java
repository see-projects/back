package dooya.see.user.application;

import dooya.see.common.UserFixture;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserUpdateCommand;
import dooya.see.user.application.service.impl.UserUpdateServiceImpl;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceTest {

    @InjectMocks
    private UserUpdateServiceImpl userUpdateService;

    @Mock
    private UserRepository userRepository;

    @DisplayName("유저 닉네임 업데이트 성공 테스트")
    @Test
    void user_nickNameUpdate_success() {
        // Arrange
        User testUser = UserFixture.testUser();
        UserUpdateCommand command = UserFixture.updateCommand();

        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));

        // Act
        UserResult result = userUpdateService.updateNickName(testUser.getEmail(), command);

        // Assert
        assertThat(result.nickName()).isEqualTo(testUser.getNickName());
    }
}
