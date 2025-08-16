package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostNotFoundException;
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
}
