package dooya.see.application.member;

import dooya.see.application.member.provided.MemberFinder;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberQueryService implements MemberFinder {
    private final MemberRepository memberRepository;

    @Override
    public Member find(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없스니다"));
    }
}
