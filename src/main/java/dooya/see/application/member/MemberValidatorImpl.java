package dooya.see.application.member;

import dooya.see.domain.shared.CustomException;
import dooya.see.domain.shared.ErrorCode;
import dooya.see.application.member.provided.MemberValidator;
import dooya.see.domain.member.Member;
import dooya.see.application.member.required.MemberRepository;
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
