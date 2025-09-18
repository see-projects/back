package dooya.see.application.member;

import dooya.see.application.member.provided.MemberFinder;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.*;
import dooya.see.domain.member.exception.DuplicateEmailException;
import dooya.see.domain.member.exception.DuplicateProfileException;
import dooya.see.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class MemberModifyService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberFinder memberFinder;

    @Override
    public Member register(MemberRegisterRequest registerRequest) {
        validateUniqueEmail(registerRequest);

        Member member = createMember(registerRequest);

        return savedMember(member);
    }

    @Override
    public Member deactivate(Long memberId) {
        Member member = findMember(memberId);

        member.deactivate();

        return savedMember(member);
    }

    @Override
    public Member updateInfo(Long memberId, MemberInfoUpdateRequest memberInfoUpdateRequest) {
        Member member = findMember(memberId);

        validateUniqueProfile(member, memberInfoUpdateRequest);

        member.updateInfo(memberInfoUpdateRequest);

        return savedMember(member);
    }

    // Member 관련 메서드
    private Member findMember(Long memberId) {
        return memberFinder.find(memberId);
    }

    private Member createMember(MemberRegisterRequest registerRequest) {
        return Member.register(registerRequest, passwordEncoder);
    }

    private Member savedMember(Member member) {
        return memberRepository.save(member);
    }

    // Email 검증 관련 메서드
    private void validateUniqueEmail(MemberRegisterRequest registerRequest) {
        Email email = createEmail(registerRequest.email());
        checkEmailNotExists(email, registerRequest.email());
    }
    private Email createEmail(String emailAddress) {
        return new Email(emailAddress);
    }

    private void checkEmailNotExists(Email email, String emailAddress) {
        if (memberRepository.findByEmail(email).isPresent())
            throw new DuplicateEmailException("이미 사용중인 이메일입니다: " + emailAddress);
    }

    // Profile 검증 관련 메서드
    private void validateUniqueProfile(Member member, MemberInfoUpdateRequest memberInfoUpdateRequest) {
        String profileAddress = memberInfoUpdateRequest.profileAddress();

        if (shouldSkipProfileValidation(profileAddress)) return;

        if (isCurrentProfile(member, profileAddress)) return;

        checkProfileNotExists(profileAddress);
    }

    private boolean shouldSkipProfileValidation(String profileAddress) {
        return profileAddress.isEmpty();
    }

    private boolean isCurrentProfile(Member member, String profileAddress) {
        Profile currentProfile = member.getDetail().getProfile();
        return currentProfile != null && currentProfile.address().equals(profileAddress);
    }

    private void checkProfileNotExists(String profileAddress) {
        Profile profile = new Profile(profileAddress);
        if (memberRepository.findByProfile(profile).isPresent())
            throw new DuplicateProfileException("이미 존재하는 프로필 주소입니다: " + profileAddress);
    }
}
