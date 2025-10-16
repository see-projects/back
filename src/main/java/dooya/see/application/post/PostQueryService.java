package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.PostRepository;
import dooya.see.application.post.required.PostSearchReader;
import dooya.see.domain.post.*;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostQueryService implements PostFinder {
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostSearchReader postSearchReader;

    @Override
    public Post find(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("게시물을 찾을 수 없습니다"));
    }

    @Override
    public Page<Post> findByMemberId(Long memberId, Pageable pageable) {
        return postRepository.findByMemberId(memberId, pageable);
    }

    @Override
    public Page<Post> findByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Post> findByStatus(PostStatus status, Pageable pageable) {
        return postRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Post> findPublicPosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
    }

    @Override
    public Page<Post> findPublicPostsByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findByCategoryAndStatus(category, PostStatus.PUBLISHED, pageable);
    }

    @Override
    public Page<Post> search(PostSearchRequest searchRequest, Pageable pageable) {
        return postRepository.searchWithOptimizedConditions(searchRequest, pageable);
    }

    @Override
    public Page<Post> findPosts(PostSearchRequest searchRequest, Pageable pageable) {
        if (requiresDefaultOrdering(searchRequest, pageable)) {
            return postRepository.findAllBy(applyDefaultOrdering(pageable));
        }

        return search(searchRequest, pageable);
    }

    @Override
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postSearchReader.searchByKeyword(keyword, pageable);
    }

    @Override
    public Post viewPost(Long postId, Long viewerId) {
        Post post = find(postId);

        // 조회 이벤트 발행 (조회수 증가)
        post.view(viewerId);

        // 도메인 이벤트 발행
        publishDomainEvents(post);

        return post;
    }

    private void publishDomainEvents(Post post) {
        post.getDomainEvents().forEach(eventPublisher::publishEvent);
        post.clearDomainEvents();
    }

    private boolean isEmptySearch(PostSearchRequest searchRequest) {
        if (searchRequest == null) {
            return true;
        }

        return searchRequest.keyword() == null
                && searchRequest.titleKeyword() == null
                && searchRequest.contentKeyword() == null
                && searchRequest.category() == null
                && searchRequest.memberId() == null
                && searchRequest.status() == null
                && searchRequest.fromDate() == null
                && searchRequest.toDate() == null;
    }

    private boolean requiresDefaultOrdering(PostSearchRequest searchRequest, Pageable pageable) {
        return isEmptySearch(searchRequest) && pageable.getSort().isUnsorted();
    }

    private Pageable applyDefaultOrdering(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );
    }
}
