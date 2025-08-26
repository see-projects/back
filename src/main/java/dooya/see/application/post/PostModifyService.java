package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
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
    private final PostLikeRepository postLikeRepository;

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

        validatePostOwnership(post, memberId, "수정");

        post.update(request);
        
        Post updatedPost = postRepository.save(post);

        publishDomainEvents(updatedPost);

        return updatedPost;
    }

    @Override
    public Post publish(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        validatePostOwnership(post, memberId, "발행");

        post.publish();
        
        Post publishedPost = postRepository.save(post);

        publishDomainEvents(publishedPost);

        return publishedPost;
    }

    @Override
    public Post hide(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        validatePostOwnership(post, memberId, "숨김 처리");

        post.hide();
        
        Post hiddenPost = postRepository.save(post);

        publishDomainEvents(hiddenPost);

        return hiddenPost;
    }

    @Override
    public Post delete(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        validatePostOwnership(post, memberId, "삭제");

        post.delete();
        
        Post deletedPost = postRepository.save(post);

        publishDomainEvents(deletedPost);

        return deletedPost;
    }

    @Override
    public Post likePost(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (isAlreadyLiked(postId, memberId)) {
            return post;
        }

        createAndSaveLike(postId, memberId);
        publishLikeEvent(post, memberId);
        
        return post;
    }

    @Override
    public Post unlikePost(Long postId, Long memberId) {
        Post post = postFinder.find(postId);

        if (isNotLiked(postId, memberId)) {
            return post;
        }

        deleteLike(postId, memberId);
        publishUnlikeEvent(post, memberId);

        return post;
    }

    private void validatePostOwnership(Post post, Long memberId, String action) {
        if (!post.isWrittenBy(memberId)) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 " + action + "할 수 있습니다.");
        }
    }

    private boolean isAlreadyLiked(Long postId, Long memberId) {
        return postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    private boolean isNotLiked(Long postId, Long memberId) {
        return !postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    private void createAndSaveLike(Long postId, Long memberId) {
        PostLike postLike = PostLike.create(postId, memberId);
        postLikeRepository.save(postLike);
    }

    private void deleteLike(Long postId, Long memberId) {
        postLikeRepository.deleteByPostIdAndMemberId(postId, memberId);
    }

    private void publishLikeEvent(Post post, Long memberId) {
        post.publishLikeEvent(memberId);
        publishDomainEvents(post);
    }

    private void publishUnlikeEvent(Post post, Long memberId) {
        post.publishUnlikeEvent(memberId);
        publishDomainEvents(post);
    }
}
