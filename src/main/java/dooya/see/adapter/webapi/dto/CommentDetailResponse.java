package dooya.see.adapter.webapi.dto;

import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentStatus;

import java.time.LocalDateTime;

public record CommentDetailResponse(
        Long commentId,
        Long postId,
        Long memberId,
        Long parentCommentId,
        String body,
        CommentStatus status,
        boolean isReply,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static CommentDetailResponse of(Comment comment) {
        return new CommentDetailResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getMemberId(),
                comment.getParentCommentId(),
                comment.getContent().text(),
                comment.getStatus(),
                comment.isReply(),
                comment.getMetaData().createdAt(),
                comment.getMetaData().modifiedAt()
        );
    }
}
