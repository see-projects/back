package dooya.see.application.member;

import dooya.see.domain.shared.UserProfileProperties;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.provided.MemberValidator;
import dooya.see.domain.member.Member;
import dooya.see.application.member.required.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberRegisterServiceImpl implements MemberRegister {

    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileProperties userProfileProperties;

    @Transactional
    @Override
    public MemberResult register(MemberRegisterCommand command) {
        memberValidator.validateDuplicateEmail(command.email());
        Member member = MemberApplicationMapper.toEntity(command, passwordEncoder, userProfileProperties.getDefaultImageUrl());
        memberRepository.save(member);

        return MemberApplicationMapper.toResult(member);
    }
}
