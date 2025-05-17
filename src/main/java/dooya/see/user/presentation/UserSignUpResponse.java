package dooya.see.user.presentation;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserSignUpResponse(
        Long id,
        String email,
        String name,
        LocalDate birthDate,
        String phoneNumber,
        Role role
) {
}
