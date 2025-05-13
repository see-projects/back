package dooya.see.user.application;

import java.time.LocalDate;

public record UserSignUpRequest(
        String email,
        String name,
        String password,
        LocalDate birthDate,
        String phoneNumber
) {
}
