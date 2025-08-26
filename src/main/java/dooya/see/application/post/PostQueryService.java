package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostQueryService implements PostFinder {
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Post find(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("게시물을 찾을 수 없습니다"));
    }

    @Override
    public List<Post> findByMemberId(Long memberId) {
        return postRepository.findByMemberId(memberId);
    }

    @Override
    public List<Post> findByCategory(PostCategory category) {
        return postRepository.findByCategory(category);
    }

    @Override
    public List<Post> findByStatus(PostStatus status) {
        return postRepository.findByStatus(status);
    }

    @Override
    public List<Post> findPublicPosts() {
        return postRepository.findByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<Post> findPublicPostsByCategory(PostCategory category) {
        return postRepository.findByCategoryAndStatus(category, PostStatus.PUBLISHED);
    }

    @Override
    public List<Post> search(PostSearchRequest searchRequest) {
        return postRepository.search(searchRequest);
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
}
