package dooya.see.adapter.webapi.dto;

import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentStatus;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long commentId,
        Long postId,
        Long memberId,
        Long parentCommentId,
        String body,
        CommentStatus status,
        LocalDateTime createdAt
) {
    public static CommentCreateResponse of(Comment comment) {
        return new CommentCreateResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getMemberId(),
                comment.getParentCommentId(),
                comment.getContent().text(),
                comment.getStatus(),
                comment.getMetaData().createdAt()
        );
    }
}
