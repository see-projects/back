package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;
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
        Post savedPost = savePost(post);

        publishDomainEvents(savedPost);

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
        return executeLikeOperation(postId, memberId, this::processLike);
    }

    @Override
    public Post unlikePost(Long postId, Long memberId) {
        return executeLikeOperation(postId, memberId, this::processUnlike);
    }

    // Post Creation 관련 메서드
    private static Post createPost(PostCreateRequest request, Long memberId) {
        return Post.create(request, memberId);
    }

    private Post savePost(Post post) {
        return postRepository.save(post);
    }

    // Post Operation 관련 메서드 (소유권 검증 필요)
    private Post executePostOperation(Long postId, Long memberId, String action, PostOperation operation) {
        Post post = findAndValidatePost(postId, memberId, action);

        operation.execute(post);
        Post savedPost = savePost(post);

        publishDomainEvents(savedPost);

        return savedPost;
    }

    // Like Operation 관련 메서드 (상호작용 권한 검증 필요)
    private Post executeLikeOperation(Long postId, Long memberId, LikeOperation operation) {
        Post post = findPost(postId);

        operation.execute(post, postId, memberId);

        publishDomainEvents(post);
        return post;
    }

    private void processLike(Post post, Long postId, Long memberId) {
        post.like(memberId);
        ensureLikeExists(postId, memberId);
    }

    private void processUnlike(Post post, Long postId, Long memberId) {
        post.unlike(memberId);
        ensureLikeRemoved(postId, memberId);
    }

    // Post 조회 관련 메서드
    private Post findPost(Long postId) {
        return postFinder.find(postId);
    }

    private Post findAndValidatePost(Long postId, Long memberId, String action) {
        Post post = findPost(postId);
        validatePostOwnership(post, memberId, action);
        return post;
    }

    // 검증 관련 메서드
    private void validatePostOwnership(Post post, Long memberId, String action) {
        if (!post.isWrittenBy(memberId))
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 " + action + "할 수 있습니다.");
    }

    // 이벤트 발행 관련 메서드
    private void publishDomainEvents(Post post) {
        post.getDomainEvents().forEach(eventPublisher::publishEvent);
        post.clearDomainEvents();
    }

    // PostLike 관리 관련 메서드
    private void ensureLikeExists(Long postId, Long memberId) {
        if (!isAlreadyLiked(postId, memberId)) {
            createLike(postId, memberId);
        }
    }

    private void ensureLikeRemoved(Long postId, Long memberId) {
        if (isAlreadyLiked(postId, memberId)) {
            removeLike(postId, memberId);
        }
    }

    private boolean isAlreadyLiked(Long postId, Long memberId) {
        return postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    private void createLike(Long postId, Long memberId) {
        PostLike postLike = PostLike.create(postId, memberId);
        postLikeRepository.save(postLike);
    }

    private void removeLike(Long postId, Long memberId) {
        postLikeRepository.deleteByPostIdAndMemberId(postId, memberId);
    }

    // Functional Interface 정의
    @FunctionalInterface
    private interface PostOperation {
        void execute(Post post);
    }

    @FunctionalInterface
    private interface LikeOperation {
        void execute(Post post, Long postId, Long memberId);
    }
}