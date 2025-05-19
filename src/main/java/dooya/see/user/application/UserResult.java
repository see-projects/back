package dooya.see.user.application;

public record UserResult(
        Long id,
        String email,
        String name,
        String nickName,
        dooya.see.user.domain.Role role
) {
}
