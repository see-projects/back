package dooya.see.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Role {
    USER("ROLE_USER", "USER"),
    ADMIN("ROLE_ADMIN","ADMIN")
    ;
    private final String roleSecurity;
    private final String roleName;

    public static Role of(String role){
        return Arrays.stream(Role.values())
                .filter(r -> r.getRoleName().equals(role) || r.getRoleSecurity().equals(role))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
