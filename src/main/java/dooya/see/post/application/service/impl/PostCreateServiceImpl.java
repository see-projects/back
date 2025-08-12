package dooya.see.post.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.post.application.dto.PostApplicationMapper;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.PostCreateService;
import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
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
