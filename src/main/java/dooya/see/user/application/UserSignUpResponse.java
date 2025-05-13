package dooya.see.user.application;

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

    public static UserSignUpResponse from(User user){
        return UserSignUpResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}
