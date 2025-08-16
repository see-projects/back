package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostQueryService implements PostFinder {
    private final PostRepository postRepository;

    @Override
    public Post find(Long postId) {
        return null;
    }

    @Override
    public boolean isWrittenBy(Long postId, Long memberId) {
        return false;
    }

    @Override
    public List<Post> findByMemberId(Long memberId) {
        return List.of();
    }

    @Override
    public List<Post> findByCategory(PostCategory category) {
        return List.of();
    }

    @Override
    public List<Post> findByStatus(PostStatus status) {
        return List.of();
    }
}
