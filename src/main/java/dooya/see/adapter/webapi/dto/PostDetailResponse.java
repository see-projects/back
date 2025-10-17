package dooya.see.adapter.webapi.dto;

import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String title,
        String body,
        PostCategory category,
        PostStatus status,
        Long authorId,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt
//        Integer viewCount,
//        Integer likeCount,
//        Integer commentCount
) {
    public static PostDetailResponse of(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getContent().title(),
                post.getContent().body(),
                post.getCategory(),
                post.getStatus(),
                post.getMemberId(),
                post.getMetaData().createdAt(),
                post.getMetaData().modifiedAt(),
                post.getMetaData().publishedAt()
        );
    }

    public static PostDetailResponse fromSearchResult(PostSearchResult result) {
        return new PostDetailResponse(
                result.id(),
                result.title(),
                result.content(),
                result.category(),
                PostStatus.PUBLISHED,
                result.memberId(),
                result.createdAt() != null ? result.createdAt().atStartOfDay() : null,
                null,
                null
        );
    }
}
