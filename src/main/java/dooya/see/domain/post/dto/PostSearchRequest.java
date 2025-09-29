package dooya.see.domain.post.dto;

import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostSearchRequest(
        String keyword,
        String titleKeyword,
        String contentKeyword,
        PostCategory category,
        Long memberId,
        PostStatus status,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {
}