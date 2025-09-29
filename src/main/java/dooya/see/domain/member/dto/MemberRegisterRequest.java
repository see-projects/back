package dooya.see.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(
        @Email String email,
        @Size(min = 3, max = 10) String nickname,
        @Size(min = 8, max = 16) String password) {
}
