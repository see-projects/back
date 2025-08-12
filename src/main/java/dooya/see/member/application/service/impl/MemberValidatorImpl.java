package dooya.see.member.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.member.application.service.MemberValidator;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberValidatorImpl implements MemberValidator {

    private final MemberRepository memberRepository;

    @Override
    public void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(MemberValidatorImpl::throwUserAlreadyExists);
    }

    private static void throwUserAlreadyExists(Member member) {
        throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
    }
}
