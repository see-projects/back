package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.common.UserFixture.*;
import static dooya.see.user.presentation.UserDtoMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class UserDtoMapperTest {

    @DisplayName("UserSignRequest를 UserSignCommand로 정상 변환한다")
    @Test
    void convert_UserSignRequest_to_UserSignCommand() {
        // Arrange
        UserSignUpRequest request = request();

        // Act
        UserSignUpCommand command = toCommand(request);

        // Assert
        assertAll(
                () -> assertThat(command.email()).isEqualTo(request.email()),
                () -> assertThat(command.name()).isEqualTo(request.name()),
                () -> assertThat(command.password()).isEqualTo(request.password()),
                () -> assertThat(command.birthDate()).isEqualTo(request.birthDate()),
                () -> assertThat(command.phoneNumber()).isEqualTo(request.phoneNumber())
        );
    }

    @DisplayName("User를 UserSignUpResponse로 정상 변환한다")
    @Test
    void convert_User_to_UserSignResponse() {
        // Arrange
        User user = testUser();

        // Act
        UserSignUpResponse response = toResponse(user);

        // Assert
        assertAll(
                () -> assertThat(response.id()).isEqualTo(user.getId()),
                () -> assertThat(response.email()).isEqualTo(user.getEmail()),
                () -> assertThat(response.name()).isEqualTo(user.getName()),
                () -> assertThat(response.birthDate()).isEqualTo(user.getBirthDate()),
                () -> assertThat(response.phoneNumber()).isEqualTo(user.getPhoneNumber()),
                () -> assertThat(response.role()).isEqualTo(user.getRole())
        );
    }
}
