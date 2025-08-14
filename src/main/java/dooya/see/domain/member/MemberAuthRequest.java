package dooya.see.domain.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberAuthRequest(
        @Email String email,
        @NotBlank String password
) {
}
