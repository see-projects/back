package dooya.see.application.post;

import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostModifyService implements PostManager {
    private final PostRepository postRepository;

    @Override
    public Post create(PostCreateRequest request, Long memberId) {
        Post post = Post.create(request, memberId);

        postRepository.save(post);

        return post;
    }

    @Override
    public Post update(Long postId, PostUpdateRequest request) {
        return null;
    }

    @Override
    public Post publish(Long postId) {
        return null;
    }

    @Override
    public Post hide(Long postId) {
        return null;
    }

    @Override
    public Post delete(Long postId) {
        return null;
    }

    @Override
    public Post incrementViewCount(Long postId) {
        return null;
    }

    @Override
    public Post incrementLikeCount(Long postId) {
        return null;
    }

    @Override
    public Post incrementCommentCount(Long postId) {
        return null;
    }
}
