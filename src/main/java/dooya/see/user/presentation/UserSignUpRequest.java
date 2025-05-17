package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder(toBuilder = true)
public record UserSignUpRequest(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다")
        @Email
        String email,

        @NotBlank(message = "이름은 비어 있을 수 없습니다")
        String name,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다")
        String password,

        @NotNull(message = "생년월일은 비어 있을 수 없습니다")
        LocalDate birthDate,

        @NotBlank(message = "전화번호는 비어 있을 수 없습니다")
        String phoneNumber
) {
}
