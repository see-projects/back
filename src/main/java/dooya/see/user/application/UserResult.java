package dooya.see.user.application;

import dooya.see.user.domain.Role;

public record UserResult(
        Long id,
        String email,
        String name,
        String nickName,
        Role role
) {
}
