package dooya.see.auth.presentation;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.presentation.dto.LoginRequest;
import dooya.see.auth.presentation.dto.LoginResponse;
import dooya.see.common.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.auth.presentation.dto.AuthDtoMapper.*;
import static dooya.see.common.AuthFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class AuthDtoMapperTest {

    @DisplayName("LoginRequest를 LoginCommand로 정산 변환한다")
    @Test
    void convert_LoginRequest_to_LoginCommand() {
        // Arrange
        LoginRequest request = request();

        // Act
        LoginCommand command = toCommand(request);

        // Assert
        assertAll(
                () -> assertThat(command.email()).isEqualTo(request.email()),
                () -> assertThat(command.password()).isEqualTo(request.password())
        );
    }

    @DisplayName("LoginResult를 LoginResponse로 정상 변환한다")
    @Test
    void convert_LoginResult_to_LoginResponse() {
        // Arrange
        LoginResult result = new LoginResult(UserFixture.testUser(), "mocked-jwt-token");

        // Act
        LoginResponse response = toResponse(result);

        // Assert
        assertAll(
                () -> assertThat(response.accessToken()).isEqualTo("mocked-jwt-token"),
                () -> assertThat(response.id()).isEqualTo(result.user().getId()),
                () -> assertThat(response.email()).isEqualTo(result.user().getEmail()),
                () -> assertThat(response.name()).isEqualTo(result.user().getName()),
                () -> assertThat(response.nickName()).isEqualTo(result.user().getNickName())
        );
    }
}
