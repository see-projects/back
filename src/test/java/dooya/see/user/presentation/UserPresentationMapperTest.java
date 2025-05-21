package dooya.see.user.presentation;

import dooya.see.user.application.dto.*;
import dooya.see.user.presentation.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.common.UserFixture.*;
import static dooya.see.user.presentation.dto.UserPresentationMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class UserPresentationMapperTest {

    @DisplayName("UserSignUpRequest를 UserSignUpCommand로 정상 변환한다")
    @Test
    void convert_UserSignRequest_to_UserSignCommand() {
        // Arrange
        UserSignUpRequest request = signUpRequest();

        // Act
        UserSignUpCommand command = toSignCommand(request);

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
        UserResult result = testUserResult();

        // Act
        UserSignUpResponse response = toSignResponse(result);

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
        UserUpdateRequest request = nickNameUpdateRequest();

        // Act
        UserUpdateCommand command = toUpdateCommand(request);

        // Assert
        assertThat(request.nickName()).isEqualTo(command.nickName());
    }

    @DisplayName("UserResult를 UserUpdateResponse로 정상 변환한다")
    @Test
    void convert_User_to_UserUpdateResponse() {
        // Arrange
        UserResult result = testUserResult();

        // Act
        UserUpdateResponse response = toUpdateResponse(result);

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
        UserResult result = testUserResult();

        // Act
        UserResponse response = toResponse(result);

        // Assert
        assertAll(
                () -> assertThat(response.email()).isEqualTo(result.email()),
                () -> assertThat(response.name()).isEqualTo(result.name()),
                () -> assertThat(response.nickName()).isEqualTo(result.nickName()),
                () -> assertThat(response.role()).isEqualTo(result.role())
        );
    }
}
