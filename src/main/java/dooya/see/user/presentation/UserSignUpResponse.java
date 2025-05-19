package dooya.see.user.presentation;

import dooya.see.user.domain.Role;
import lombok.Builder;

@Builder
public record UserSignUpResponse(
        Long id,
        String email,
        String name,
        String nickName,
        Role role
) {
}
