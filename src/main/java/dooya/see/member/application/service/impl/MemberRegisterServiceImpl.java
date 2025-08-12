package dooya.see.member.application.service.impl;

import dooya.see.common.config.UserProfileProperties;
import dooya.see.member.application.dto.MemberApplicationMapper;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.dto.MemberRegisterCommand;
import dooya.see.member.application.service.MemberRegisterService;
import dooya.see.member.application.service.MemberValidator;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberRegisterServiceImpl implements MemberRegisterService {

    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileProperties userProfileProperties;

    @Transactional
    @Override
    public MemberResult userSignUp(MemberRegisterCommand command) {
        memberValidator.validateDuplicateEmail(command.email());
        Member member = MemberApplicationMapper.toEntity(command, passwordEncoder, userProfileProperties.getDefaultImageUrl());
        memberRepository.save(member);

        return MemberApplicationMapper.toResult(member);
    }
}
