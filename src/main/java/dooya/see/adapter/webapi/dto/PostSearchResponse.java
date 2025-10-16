package dooya.see.adapter.webapi.dto;

import dooya.see.application.post.dto.PostSearchResult;

public record PostSearchResponse(
        Long id,
        String title,
        String content,
        String category,
        String authorNickname
) {
    public static PostSearchResponse from(PostSearchResult result) {
        return new PostSearchResponse(
                result.id(),
                result.title(),
                result.content(),
                result.category() != null ? result.category().name() : null,
                result.authorNickname()
        );
    }
}
