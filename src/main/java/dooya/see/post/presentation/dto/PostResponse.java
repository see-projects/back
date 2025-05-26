package dooya.see.post.presentation.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String nickName,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
