package dooya.see.auth.application;

import lombok.Builder;

@Builder
public record LoginCommand(
        String email,
        String password
) {
}
