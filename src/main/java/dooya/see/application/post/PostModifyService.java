package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
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
        Post post = createPost(request, memberId);
        Post savedPost = savedPost(post);

        publishCreationAndDomainEvents(savedPost);

        return savedPost;
    }

    @Override
    public Post update(PostUpdateRequest request, Long postId, Long memberId) {
        return executePostOperation(postId, memberId, "수정",
                post -> post.update(request));
    }

    @Override
    public Post publish(Long postId, Long memberId) {
        return executePostOperation(postId, memberId, "발행",
                Post::publish);
    }

    @Override
    public Post hide(Long postId, Long memberId) {
        return executePostOperation(postId, memberId, "숨김 처리",
                Post::hide);
    }

    @Override
    public Post delete(Long postId, Long memberId) {
        return executePostOperation(postId, memberId, "삭제",
                Post::delete);
    }

    @Override
    public Post likePost(Long postId, Long memberId) {
        Post post = findPost(postId);

        if (isAlreadyLiked(postId, memberId))
            return post;

        createLike(postId, memberId);
        publishLikeEvent(post, memberId);

        return post;
    }

    @Override
    public Post unlikePost(Long postId, Long memberId) {
        Post post = findPost(postId);

        if (isNotLiked(postId, memberId))
            return post;

        removeLike(postId, memberId);
        publishUnlikeEvent(post, memberId);

        return post;
    }

    // Post Creation 관련 메서드
    private static Post createPost(PostCreateRequest request, Long memberId) {
        return Post.create(request, memberId);
    }

    private Post savedPost(Post post) {
        return postRepository.save(post);
    }

    // Post Operation 관련 메서드
    private Post executePostOperation(Long postId, Long memberId, String action, PostOperation operation) {
        Post post = findAndValidatePost(postId, memberId, action);

        operation.execute(post);
        Post savedPost = savedPost(post);

        publishDomainEvents(savedPost);

        return savedPost;
    }

    private Post findPost(Long postId) {
        return postFinder.find(postId);
    }

    private Post findAndValidatePost(Long postId, Long memberId, String action) {
        Post post = findPost(postId);
        validatePostOwnership(post, memberId, action);

        return post;
    }

    private void validatePostOwnership(Post post, Long memberId, String action) {
        if (!post.isWrittenBy(memberId))
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 " + action + "할 수 있습니다.");
    }

    private void publishDomainEvents(Post post) {
        post.getDomainEvents().forEach(eventPublisher::publishEvent);
        post.clearDomainEvents();
    }

    // Like 관련 메서드
    private boolean isAlreadyLiked(Long postId, Long memberId) {
        return postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    private boolean isNotLiked(Long postId, Long memberId) {
        return !postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    private void createLike(Long postId, Long memberId) {
        PostLike postLike = PostLike.create(postId, memberId);
        postLikeRepository.save(postLike);
    }

    private void removeLike(Long postId, Long memberId) {
        postLikeRepository.deleteByPostIdAndMemberId(postId, memberId);
    }

    private void publishLikeEvent(Post post, Long memberId) {
        post.like(memberId);
        publishDomainEvents(post);
    }

    private void publishUnlikeEvent(Post post, Long memberId) {
        post.unlike(memberId);
        publishDomainEvents(post);
    }

    // Event Publishing 관련 메서드
    private void publishCreationAndDomainEvents(Post post) {
        post.publishCreationEventIfNeeded();
        publishDomainEvents(post);
    }

    @FunctionalInterface
    private interface PostOperation {

        void execute(Post post);
    }
}
