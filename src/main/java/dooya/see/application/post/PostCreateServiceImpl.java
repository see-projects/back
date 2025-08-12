package dooya.see.application.post;

import dooya.see.domain.shared.CustomException;
import dooya.see.domain.shared.ErrorCode;
import dooya.see.domain.post.PostApplicationMapper;
import dooya.see.domain.post.PostCommand;
import dooya.see.domain.post.PostResult;
import dooya.see.application.post.provided.PostCreateService;
import dooya.see.domain.post.Post;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.member.Member;
import dooya.see.application.member.required.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCreateServiceImpl implements PostCreateService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    @Override
    public PostResult createPost(String email, PostCommand command) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = PostApplicationMapper.toEntity(command, member);
        postRepository.save(post);

        return PostApplicationMapper.toResult(post);
    }
}
