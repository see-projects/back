package dooya.see.application.member;

import dooya.see.domain.shared.CustomException;
import dooya.see.domain.shared.ErrorCode;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.Role;
import dooya.see.application.member.required.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQueryService implements MemberFinder {
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public Member find(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    @Transactional
    @Override
    public List<Member> findMembers() {
        List<Member> members = memberRepository.findByRole(Role.USER);

        return MemberApplicationMapper.toResults(members);
    }
}
