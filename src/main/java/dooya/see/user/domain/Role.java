package dooya.see.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    USER("ROLE_USER", "USER"),
    ADMIN("ROLE_ADMIN","ADMIN")
    ;
    private final String roleSecurity;
    private final String roleName;

}
