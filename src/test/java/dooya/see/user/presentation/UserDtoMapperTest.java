package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.application.UserUpdateCommand;
import dooya.see.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.common.UserFixture.*;
import static dooya.see.user.presentation.UserDtoMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class UserDtoMapperTest {

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
        User user = testUser();

        // Act
        UserSignUpResponse response = toSignResponse(user);

        // Assert
        assertAll(
                () -> assertThat(response.id()).isEqualTo(user.getId()),
                () -> assertThat(response.email()).isEqualTo(user.getEmail()),
                () -> assertThat(response.name()).isEqualTo(user.getName()),
                () -> assertThat(response.nickName()).isEqualTo(user.getNickName()),
                () -> assertThat(response.role()).isEqualTo(user.getRole())
        );
    }

    @DisplayName("UserUpdateRequest를 UserUpdateCommand로 정상 변환한다")
    @Test
    void convert_UserUpdateRequest_to_UserUpdateCommand() {
        // Arrange
        UserUpdateRequest request = updateRequest();

        // Act
        UserUpdateCommand command = toUpdateCommand(request);

        // Assert
        assertThat(request.nickName()).isEqualTo(command.nickName());
    }

    @DisplayName("User를 UserUpdateResponse로 정상 변환한다")
    @Test
    void convert_User_to_UserUpdateResponse() {
        // Arrange
        User user = testUser();

        // Act
        UserUpdateResponse response = toUpdateResponse(user);

        // Assert
        assertThat(response.nickName()).isEqualTo(user.getNickName()); 
    }
}
