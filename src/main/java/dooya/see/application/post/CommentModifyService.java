package dooya.see.application.post;

import dooya.see.application.post.provided.CommentFinder;
import dooya.see.application.post.provided.CommentManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.*;
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
        postFinder.find(postId);

        Comment comment = Comment.create(request, postId, memberId);
        Comment savedComment = commentRepository.save(comment);

        savedComment.publishCreationEventIfNeeded();
        publishDomainEvents(savedComment);

        return savedComment;
    }

    @Override
    public Comment update(CommentUpdateRequest request, Long commentId, Long memberId) {
        Comment comment = findCommentByIdAndValidateOwnership(commentId, memberId);

        comment.update(request);
        Comment updatedComment = commentRepository.save(comment);

        publishDomainEvents(updatedComment);

        return updatedComment;
    }

    @Override
    public Comment delete(Long commentId, Long memberId) {
        Comment comment = findCommentByIdAndValidateOwnership(commentId, memberId);

        comment.delete();
        Comment deletedComment = commentRepository.save(comment);

        publishDomainEvents(deletedComment);

        return deletedComment;
    }

    @Override
    public Comment hide(Long commentId, Long memberId) {
        Comment comment = findCommentByIdAndValidateOwnership(commentId, memberId);

        comment.hide();
        Comment hiddenComment = commentRepository.save(comment);

        publishDomainEvents(hiddenComment);

        return hiddenComment;
    }

    private void publishDomainEvents(Comment comment) {
        comment.getDomainEvents().forEach(eventPublisher::publishEvent);
        comment.clearDomainEvents();
    }

    private Comment findCommentByIdAndValidateOwnership(Long commentId, Long memberId) {
        Comment comment = commentFinder.find(commentId);

        if (!comment.isWrittenBy(memberId)) {
            throw UnauthorizedCommentAccessException.forAction("수정");
        }

        return comment;
    }

}
