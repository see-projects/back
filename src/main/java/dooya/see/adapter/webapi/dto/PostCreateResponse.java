package dooya.see.adapter.webapi.dto;

import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;

import java.time.LocalDateTime;

public record PostCreateResponse(
        Long postId,
        String title,
        PostCategory category,
        PostStatus status,
        LocalDateTime createdAt
) {
}
