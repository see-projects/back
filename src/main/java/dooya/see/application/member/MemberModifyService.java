package dooya.see.application.member;

import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.DuplicateEmailException;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.member.PasswordEncoder;
import dooya.see.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberModifyService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member register(MemberRegisterRequest registerRequest) {
        if (memberRepository.findByEmail(new Email(registerRequest.email())).isPresent()) {
            throw new DuplicateEmailException("이미 사용중인 이메일입니다: " + registerRequest.email());
        }

        Member member = Member.register(registerRequest, passwordEncoder);

        memberRepository.save(member);

        return member;
    }
}
