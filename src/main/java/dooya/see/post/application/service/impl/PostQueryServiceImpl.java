package dooya.see.post.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.post.application.dto.PostApplicationMapper;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.PostQueryService;
import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    @Override
    public List<PostResult> getPosts() {
        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUNT);
        }

        return PostApplicationMapper.toResults(posts);
    }
}
