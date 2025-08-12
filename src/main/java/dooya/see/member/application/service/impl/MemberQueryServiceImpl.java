package dooya.see.member.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.member.application.dto.MemberApplicationMapper;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.service.MemberQueryService;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.Role;
import dooya.see.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public MemberResult getUserByEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return MemberApplicationMapper.toResult(member);
    }

    @Transactional
    @Override
    public List<MemberResult> getUsers() {
        List<Member> members = memberRepository.findByRole(Role.USER);

        return MemberApplicationMapper.toResults(members);
    }
}
