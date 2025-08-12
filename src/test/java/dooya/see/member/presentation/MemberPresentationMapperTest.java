package dooya.see.member.presentation;

import dooya.see.member.application.dto.*;
import dooya.see.member.presentation.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.common.UserFixture.*;
import static dooya.see.member.presentation.dto.MemberPresentationMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MemberPresentationMapperTest {

    @DisplayName("UserSignUpRequest를 UserSignUpCommand로 정상 변환한다")
    @Test
    void convert_UserSignRequest_to_UserSignCommand() {
        // Arrange
        MemberRegisterRequest request = signUpRequest();

        // Act
        MemberRegisterCommand command = toSignCommand(request);

        // Assert
        assertAll(
                () -> assertThat(command.email()).isEqualTo(request.email()),
                () -> assertThat(command.name()).isEqualTo(request.name()),
                () -> assertThat(command.password()).isEqualTo(request.password()),
                () -> assertThat(command.nickName()).isEqualTo(request.nickName())
        );
    }

    @DisplayName("User를 UserSignUpResponse로 정상 변환한다")
    @Test
    void convert_User_to_UserSignResponse() {
        // Arrange
        MemberResult result = testUserResult();

        // Act
        MemberRegisterResponse response = toSignResponse(result);

        // Assert
        assertAll(
                () -> assertThat(response.id()).isEqualTo(result.id()),
                () -> assertThat(response.email()).isEqualTo(result.email()),
                () -> assertThat(response.name()).isEqualTo(result.name()),
                () -> assertThat(response.nickName()).isEqualTo(result.nickName()),
                () -> assertThat(response.role()).isEqualTo(result.role())
        );
    }

    @DisplayName("UserUpdateRequest를 UserUpdateCommand로 정상 변환한다")
    @Test
    void convert_UserUpdateRequest_to_UserUpdateCommand() {
        // Arrange
        NickNameUpdateRequest request = nickNameUpdateRequest();

        // Act
        NickNameUpdateCommand command = toUpdateCommand(request);

        // Assert
        assertThat(request.nickName()).isEqualTo(command.nickName());
    }

    @DisplayName("UserResult를 UserUpdateResponse로 정상 변환한다")
    @Test
    void convert_User_to_UserUpdateResponse() {
        // Arrange
        MemberResult result = testUserResult();

        // Act
        NickNameUpdateResponse response = toUpdateResponse(result);

        // Assert
        assertThat(response.nickName()).isEqualTo(result.nickName());
    }

    @DisplayName("PasswordUpdateRequest를 PasswordUpdateCommand로 정상 반환한다")
    @Test
    void convert_PasswordUpdateRequest_to_PasswordUpdateCommand() {
        // Arrange
        PasswordUpdateRequest request = passwordUpdateRequest();

        // Act
        PasswordUpdateCommand command = toPasswordUpdateCommand(request);

        // Assert
        assertThat(request.currentPassword()).isEqualTo(command.currentPassword());
        assertThat(request.newPassword()).isEqualTo(command.newPassword());
    }

    @DisplayName("PasswordUpdateResult를 PasswordUpdateResponse로 정상 반환한다")
    @Test
    void convert_PasswordUpdateResponse_to_PasswordUpdateResponse() {
        // Arrange
        PasswordUpdateResult result = passwordUpdateResult();

        // Act
        PasswordUpdateResponse response = toPasswordUpdateResponse(result);

        // Assert
        assertThat(response.message()).isEqualTo(result.message());
    }

    @DisplayName("UserResult를 UserResponse로 정상 반환한다")
    @Test
    void convert_UserResult_to_UserResponse() {
        // Arrange
        MemberResult result = testUserResult();

        // Act
        MemberResponse response = toResponse(result);

        // Assert
        assertAll(
                () -> assertThat(response.email()).isEqualTo(result.email()),
                () -> assertThat(response.name()).isEqualTo(result.name()),
                () -> assertThat(response.nickName()).isEqualTo(result.nickName()),
                () -> assertThat(response.role()).isEqualTo(result.role())
        );
    }
}
