package dooya.see.application.post;

import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostNotFoundException;
import dooya.see.domain.post.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostModifyService implements PostManager {
    private final PostRepository postRepository;
    private final PostFinder postFinder;

    @Override
    public Post create(PostCreateRequest request, Long memberId) {
        Post post = Post.create(request, memberId);

        postRepository.save(post);

        return post;
    }

    @Override
    public Post update(PostUpdateRequest request, Long postId) {
        Post post = postFinder.find(postId);

        post.update(request);

        return postRepository.save(post);
    }

    @Override
    public Post publish(Long postId) {
        Post post = postFinder.find(postId);

        post.publish();

        return postRepository.save(post);
    }

    @Override
    public Post hide(Long postId) {
        Post post = postFinder.find(postId);

        post.hide();

        return postRepository.save(post);
    }

    @Override
    public Post delete(Long postId) {
        Post post = postFinder.find(postId);

        post.delete();

        return postRepository.save(post);
    }

    @Override
    public Post incrementViewCount(Long postId) {
        Post post = postFinder.find(postId);

        post.incrementViewCount();

        return postRepository.save(post);
    }

    @Override
    public Post incrementLikeCount(Long postId) {
        Post post = postFinder.find(postId);

        post.incrementLikeCount();

        return postRepository.save(post);
    }

    @Override
    public Post incrementCommentCount(Long postId) {
        Post post = postFinder.find(postId);

        post.incrementCommentCount();

        return postRepository.save(post);
    }
}
