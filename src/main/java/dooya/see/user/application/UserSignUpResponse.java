package dooya.see.user.application;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserSignUpResponse(
        Long id,
        String email,
        String name,
        LocalDate birthDate,
        String phoneNumber
) {
}
