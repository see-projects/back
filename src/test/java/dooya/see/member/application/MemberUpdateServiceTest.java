package dooya.see.member.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.common.s3.S3Uploader;
import dooya.see.member.application.dto.PasswordUpdateCommand;
import dooya.see.member.application.dto.PasswordUpdateResult;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.dto.NickNameUpdateCommand;
import dooya.see.member.application.service.impl.MemberUpdateServiceImpl;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
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
public class MemberUpdateServiceTest {

    @InjectMocks
    private MemberUpdateServiceImpl userUpdateService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Uploader s3Uploader;

    private final Member testMember = testUser();

    @DisplayName("유저 닉네임 업데이트 성공 테스트")
    @Test
    void user_NickNameUpdate_Success() {
        // Arrange
        NickNameUpdateCommand command = updateCommand();

        given(memberRepository.findByEmail(testMember.getEmail())).willReturn(Optional.of(testMember));

        // Act
        MemberResult result = userUpdateService.updateNickName(testMember.getEmail(), command);

        // Assert
        assertThat(result.nickName()).isEqualTo(testMember.getNickName());
    }

    @DisplayName("유저 닉네임 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_NickNameUpdate_Failure() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(memberRepository).findByEmail(testMember.getEmail());

        // Act & Assert
        assertThatCode(() -> userUpdateService.updateNickName(testMember.getEmail(), updateCommand()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
    }

    @DisplayName("유저 비밀번호 업데이트 성공 테스트")
    @Test
    void user_PasswordUpdate_Success() {
        // Arrange
        PasswordUpdateCommand command = passwordUpdateCommand();

        given(memberRepository.findByEmail(testMember.getEmail())).willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(command.currentPassword(), testMember.getPassword())).willReturn(true);
        given(passwordEncoder.encode(command.newPassword())).willReturn("updatePassword");

        // Act
        PasswordUpdateResult result = userUpdateService.updatePassword(testMember.getEmail(), command);

        // Assert
        assertThat(result.message()).isEqualTo("비밀번호가 성공적으로 변경되었습니다.");
        assertThat(testMember.getPassword()).isEqualTo("updatePassword");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
        then(passwordEncoder).should(times(1)).encode(command.newPassword());
    }

    @DisplayName("유저 비밀번호 업데이트 실패 테스트 - 일치하지 않는 비밀번호")
    @Test
    void user_PasswordUpdate_Failure() {
        // Arrange
        PasswordUpdateCommand command = passwordUpdateCommand();

        // Act & Assert
        assertThatCode(() -> userUpdateService.updatePassword(testMember.getEmail(), command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("비밀번호 정보가 일치하지 않습니다.");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
    }

    @DisplayName("유저 프로필 이미지 업데이트 성공 테스트")
    @Test
    void user_ProfileImageUpdate_Success() {
        // Arrange
        String email = "test@example.com";
        String expectedImageUrl = "https://s3.amazon.com/profile/image.png";

        MultipartFile mockFile = mock(MultipartFile.class);
        Member testMember = testUser();

        given(s3Uploader.upload(mockFile, "profile")).willReturn(expectedImageUrl);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));

        // Act
        MemberResult result = userUpdateService.updateProfileImage(email, mockFile);

        // Assert
        assertThat(result.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(testMember.getProfileImageUrl()).isEqualTo(expectedImageUrl);

        then(s3Uploader).should(times(1)).upload(mockFile, "profile");
        then(memberRepository).should(times(1)).findByEmail(email);
    }

    @DisplayName("유저 프로필 이미지 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_ProfileImageUpdate_Fail() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(memberRepository).findByEmail(testMember.getEmail());

        // Act & Assert
        assertThatCode(() -> userUpdateService.updateProfileImage(testMember.getEmail(), mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
    }

    @DisplayName("유저 프로필 업데이트 실패 테스트 - 유저가 존재하지 않음")
    @Test
    void user_ProfileUpdate_Fail() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        NickNameUpdateCommand command = updateCommand();
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(memberRepository).findByEmail(testMember.getEmail());
        // Act && Assert
        assertThatCode(() -> userUpdateService.updateProfile(testMember.getEmail(), command, mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
    }

    @DisplayName("유저 프로필 업데이트 성공 테스트")
    @Test
    void user_ProfileUpdate_Success() {
        // Arrange
        String email = "test@example.com";
        String expectedImageUrl = "https://s3.amazon.com/profile/image.png";
        NickNameUpdateCommand command = updateCommand();

        MultipartFile mockFile = mock(MultipartFile.class);
        Member testMember = testUser();

        given(s3Uploader.upload(mockFile, "profile")).willReturn(expectedImageUrl);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));

        // Act
        MemberResult result = userUpdateService.updateProfile(email, command, mockFile);

        // Assert
        assertThat(result.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(testMember.getProfileImageUrl()).isEqualTo(expectedImageUrl);

        then(s3Uploader).should(times(1)).upload(mockFile, "profile");
        then(memberRepository).should(times(1)).findByEmail(email);
    }
}
