package dooya.see.user.application;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserSignUpRequest(
        String email,
        String name,
        String password,
        LocalDate birthDate,
        String phoneNumber
) {
}
