package dooya.see.user.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.common.s3.S3Uploader;
import dooya.see.user.application.dto.PasswordUpdateCommand;
import dooya.see.user.application.dto.PasswordUpdateResult;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.NickNameUpdateCommand;
import dooya.see.user.application.service.impl.UserUpdateServiceImpl;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static dooya.see.common.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceTest {

    @InjectMocks
    private UserUpdateServiceImpl userUpdateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Uploader s3Uploader;

    private final User testUser = testUser();

    @DisplayName("유저 닉네임 업데이트 성공 테스트")
    @Test
    void user_nickNameUpdate_success() {
        // Arrange
        NickNameUpdateCommand command = updateCommand();

        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));

        // Act
        UserResult result = userUpdateService.updateNickName(testUser.getEmail(), command);

        // Assert
        assertThat(result.nickName()).isEqualTo(testUser.getNickName());
    }

    @DisplayName("유저 닉네임 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_nickNameUpdate_failure() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(userRepository).findByEmail(testUser.getEmail());

        // Act & Assert
        assertThatCode(() -> userUpdateService.updateNickName(testUser.getEmail(), updateCommand()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
    }

    @DisplayName("유저 비밀번호 업데이트 성공 테스트")
    @Test
    void user_passwordUpdate_success() {
        // Arrange
        PasswordUpdateCommand command = passwordUpdateCommand();

        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(command.currentPassword(), testUser.getPassword())).willReturn(true);
        given(passwordEncoder.encode(command.newPassword())).willReturn("updatePassword");

        // Act
        PasswordUpdateResult result = userUpdateService.updatePassword(testUser.getEmail(), command);

        // Assert
        assertThat(result.message()).isEqualTo("비밀번호가 성공적으로 변경되었습니다.");
        assertThat(testUser.getPassword()).isEqualTo("updatePassword");

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
        then(passwordEncoder).should(times(1)).encode(command.newPassword());
    }

    @DisplayName("유저 비밀번호 업데이트 실패 테스트 - 일치하지 않는 비밀번호")
    @Test
    void user_passwordUpdate_failure() {
        // Arrange
        PasswordUpdateCommand command = passwordUpdateCommand();

        // Act & Assert
        assertThatCode(() -> userUpdateService.updatePassword(testUser.getEmail(), command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("비밀번호 정보가 일치하지 않습니다.");

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
    }

    @DisplayName("유저 프로필 이미지 업데이트 성공 테스트")
    @Test
    void user_profileImageUpdate_success() {
        // Arrange
        String email = "test@example.com";
        String expectedImageUrl = "https://s3.amazon.com/profile/image.png";

        MultipartFile mockFile = mock(MultipartFile.class);
        User testUser = testUser();

        given(s3Uploader.upload(mockFile, "profile")).willReturn(expectedImageUrl);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));

        // Act
        UserResult result = userUpdateService.updateProfileImage(email, mockFile);

        // Assert
        assertThat(result.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(testUser.getProfileImageUrl()).isEqualTo(expectedImageUrl);

        then(s3Uploader).should(times(1)).upload(mockFile, "profile");
        then(userRepository).should(times(1)).findByEmail(email);
    }

    @DisplayName("유저 프로필 이미지 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_profileImageUpdate_fail() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(userRepository).findByEmail(testUser.getEmail());

        // Act & Assert
        assertThatCode(() -> userUpdateService.updateProfileImage(testUser.getEmail(), mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
    }

    @DisplayName("유저 프로필 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_profileUpdate_fail() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        NickNameUpdateCommand command = updateCommand();
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(userRepository).findByEmail(testUser.getEmail());
        // Act && Assert
        assertThatCode(() -> userUpdateService.updateProfile(testUser.getEmail(), command, mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
    }

    @DisplayName("유저 프로필 업데이트 성공 테스트")
    @Test
    void user_profileUpdate_success() {
        // Arrange
        String email = "test@example.com";
        String expectedImageUrl = "https://s3.amazon.com/profile/image.png";
        NickNameUpdateCommand command = updateCommand();

        MultipartFile mockFile = mock(MultipartFile.class);
        User testUser = testUser();

        given(s3Uploader.upload(mockFile, "profile")).willReturn(expectedImageUrl);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));

        // Act
        UserResult result = userUpdateService.updateProfile(email, command, mockFile);

        // Assert
        assertThat(result.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(testUser.getProfileImageUrl()).isEqualTo(expectedImageUrl);

        then(s3Uploader).should(times(1)).upload(mockFile, "profile");
        then(userRepository).should(times(1)).findByEmail(email);
    }
}
