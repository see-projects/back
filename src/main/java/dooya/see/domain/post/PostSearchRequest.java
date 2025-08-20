package dooya.see.domain.post;

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