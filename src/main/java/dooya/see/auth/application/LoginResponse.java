package dooya.see.auth.application;

import dooya.see.user.application.UserSignUpResponse;
import dooya.see.user.domain.User;
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

    public static LoginResponse from(User user, String accessToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
