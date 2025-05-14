package dooya.see.auth.application;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LoginResponse(
        String accessToken,
        Long id,
        String email,
        String name,
        LocalDate birthDate,
        String phoneNumber
) {
}
