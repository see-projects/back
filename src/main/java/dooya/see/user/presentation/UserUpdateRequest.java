package dooya.see.user.presentation;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
        String nickName
) {
}
