package dooya.see.domain.post;

import java.time.LocalDateTime;

public record PostSearchCondition(
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
