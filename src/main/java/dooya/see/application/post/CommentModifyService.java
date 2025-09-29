package dooya.see.application.post;

import dooya.see.application.post.provided.CommentFinder;
import dooya.see.application.post.provided.CommentManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.dto.CommentCreateRequest;
import dooya.see.domain.post.dto.CommentUpdateRequest;
import dooya.see.domain.post.exception.UnauthorizedCommentAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentModifyService implements CommentManager {
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Override
    public Comment create(CommentCreateRequest request, Long postId, Long memberId) {
        validatePostExists(postId);

        Comment comment = createComment(request, postId, memberId);
        Comment savedComment = savedComment(comment);

        publishCreationAndDomainEvents(savedComment);

        return savedComment;
    }

    @Override
    public Comment update(CommentUpdateRequest request, Long commentId, Long memberId) {
        return executeCommentOperation(commentId, memberId, "수정",
                comment -> comment.update(request));
    }

    @Override
    public Comment delete(Long commentId, Long memberId) {
        return executeCommentOperation(commentId, memberId, "삭제",
                Comment::delete);
    }

    @Override
    public Comment hide(Long commentId, Long memberId) {
        return executeCommentOperation(commentId, memberId, "숨김",
                Comment::hide);
    }

    // Comment Creation 관련 메서드
    private void validatePostExists(Long postId) {
        postFinder.find(postId);
    }

    private static Comment createComment(CommentCreateRequest request, Long postId, Long memberId) {
        return Comment.create(request, postId, memberId);
    }

    private Comment savedComment(Comment comment) {
        return commentRepository.save(comment);
    }

    private void publishCreationAndDomainEvents(Comment comment) {
        comment.publishCreationEventIfNeeded();
        publishDomainEvents(comment);
    }

    // Comment Operation 관련 메서드
    private Comment executeCommentOperation(Long commentId, Long memberId, String action, CommentOperation operation) {
        Comment comment = findAndValidateComment(commentId, memberId, action);

        operation.execute(comment);
        Comment savedComment = savedComment(comment);

        publishDomainEvents(savedComment);

        return savedComment;
    }

    private Comment findAndValidateComment(Long commentId, Long memberId, String action) {
        Comment comment = commentFinder.find(commentId);
        validateCommentOwnership(memberId, action, comment);

        return comment;
    }

    private static void validateCommentOwnership(Long memberId, String action, Comment comment) {
        if (!comment.isWrittenBy(memberId))
            throw UnauthorizedCommentAccessException.forAction(action);
    }

    // Event Publishing 관련 메서드
    private void publishDomainEvents(Comment comment) {
        comment.getDomainEvents().forEach(eventPublisher::publishEvent);
        comment.clearDomainEvents();
    }

    @FunctionalInterface
    private interface CommentOperation {
        void execute(Comment comment);
    }
}
