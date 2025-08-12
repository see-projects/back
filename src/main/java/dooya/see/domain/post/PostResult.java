package dooya.see.domain.post;

import java.time.LocalDateTime;

public record PostResult(
        Long id,
        String nickName,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
