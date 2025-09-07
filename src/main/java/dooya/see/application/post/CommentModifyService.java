package dooya.see.application.post;

import dooya.see.application.post.provided.CommentManager;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentCreateRequest;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostNotFoundException;
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

    private void publishDomainEvents(Comment comment) {
        comment.getDomainEvents().forEach(eventPublisher::publishEvent);
        comment.clearDomainEvents();
    }
}
