package dooya.see.user.presentation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder(toBuilder = true)
public record UserSignUpRequest(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다")
        @Email
        String email,

        @NotBlank(message = "이름은 비어 있을 수 없습니다")
        String name,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다")
        String password,

        @NotBlank(message = "닉네임은 비어 있을 수 없습니다")
        String nickName
) {
}
