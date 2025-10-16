package dooya.see.application.post.dto;

import dooya.see.domain.post.PostCategory;

import java.time.LocalDate;
import java.util.List;

public record PostSearchResult(
        Long id,
        String title,
        String content,
        PostCategory category,
        Long memberId,
        String authorNickname,
        List<String> tags,
        LocalDate createdAt
) {
}
