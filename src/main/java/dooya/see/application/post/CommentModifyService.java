package dooya.see.application.post;

import dooya.see.application.post.provided.CommentManager;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentModifyService implements CommentManager {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Comment create(CommentCreateRequest request, Long postId, Long memberId) {
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다"));

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

    private void publishDomainEvents(Comment comment) {
        comment.getDomainEvents().forEach(eventPublisher::publishEvent);
        comment.clearDomainEvents();
    }

    private Comment findCommentByIdAndValidateOwnership(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        if (!comment.isWrittenBy(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다");
        }

        return comment;
    }

}
