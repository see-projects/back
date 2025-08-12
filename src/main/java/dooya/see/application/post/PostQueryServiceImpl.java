package dooya.see.application.post;

import dooya.see.domain.shared.CustomException;
import dooya.see.domain.shared.ErrorCode;
import dooya.see.domain.post.PostApplicationMapper;
import dooya.see.domain.post.PostResult;
import dooya.see.application.post.provided.PostQueryService;
import dooya.see.domain.post.Post;
import dooya.see.application.post.required.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<PostResult> getPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);

        return PostApplicationMapper.toResults(posts);
    }

    @Transactional(readOnly = true)
    @Override
    public PostResult getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return PostApplicationMapper.toResult(post);
    }
}
