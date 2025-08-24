package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostUpdateRequest;
import dooya.see.domain.post.UnauthorizedPostAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostModifyService implements PostManager {
    private final PostRepository postRepository;
    private final PostFinder postFinder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Post create(PostCreateRequest request, Long memberId) {
        Post post = Post.create(request, memberId);

        postRepository.save(post);

        publishDomainEvents(post);

        return post;
    }

    private void publishDomainEvents(Post post) {
        post.getDomainEvents().forEach(eventPublisher::publishEvent);
        post.clearDomainEvents();
    }

    @Override
    public Post update(PostUpdateRequest request, Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.update(request);

        return postRepository.save(post);
    }

    @Override
    public Post publish(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 발행할 수 있습니다.");
        }

        post.publish();

        return postRepository.save(post);
    }

    @Override
    public Post hide(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 숨김 처리할 수 있습니다.");
        }

        post.hide();

        return postRepository.save(post);
    }

    @Override
    public Post delete(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        post.delete();

        return postRepository.save(post);
    }
}
