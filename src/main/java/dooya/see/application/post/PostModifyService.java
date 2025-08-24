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
        Post savedPost = postRepository.save(post);

        savedPost.publishCreationEventIfNeeded();
        publishDomainEvents(savedPost);

        return savedPost;
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
        
        Post updatedPost = postRepository.save(post);

        publishDomainEvents(updatedPost);

        return updatedPost;
    }

    @Override
    public Post publish(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 발행할 수 있습니다.");
        }

        post.publish();
        
        Post publishedPost = postRepository.save(post);

        publishDomainEvents(publishedPost);

        return publishedPost;
    }

    @Override
    public Post hide(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 숨김 처리할 수 있습니다.");
        }

        post.hide();
        
        Post hiddenPost = postRepository.save(post);

        publishDomainEvents(hiddenPost);

        return hiddenPost;
    }

    @Override
    public Post delete(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        post.delete();
        
        Post deletedPost = postRepository.save(post);

        publishDomainEvents(deletedPost);

        return deletedPost;
    }

    @Override
    public Post likePost(Long postId, Long memberId) {
        Post post = postFinder.find(postId);
        
        post.like(memberId);
        
        publishDomainEvents(post);
        
        return post;
    }

    @Override
    public Post unlikePost(Long postId, Long memberId) {
        Post post = postFinder.find(postId);
        
        post.unlike(memberId);
        
        publishDomainEvents(post);
        
        return post;
    }
}
