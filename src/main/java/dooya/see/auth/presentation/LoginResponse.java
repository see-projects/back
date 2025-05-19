package dooya.see.auth.presentation;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        Long id,
        String email,
        String name,
        String nickName
) {
}
