package dooya.see.domain.member;

import dooya.see.domain.AbstractEntity;
import dooya.see.domain.member.dto.MemberInfoUpdateRequest;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import dooya.see.domain.shared.Email;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@ToString(callSuper = true, exclude = "detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends AbstractEntity {
    @NaturalId
    @Embedded
    private Email email;

    private String nickname;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private MemberDetail detail;

    public static Member register(MemberRegisterRequest registerRequest, PasswordEncoder passwordEncoder) {
        Member member = new Member();

        member.initializeBasicInfo(registerRequest);
        member.encodePassword(registerRequest.password(), passwordEncoder);
        member.activateMember();
        member.createMemberDetail();

        return member;
    }

    public boolean verifyPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.passwordHash);
    }

    public void changePassword(String password, PasswordEncoder passwordEncoder) {
        encodePassword(password, passwordEncoder);
    }

    public void deactivate() {
        changeStatusToDeactivated();
        deactivateMemberDetail();
    }

    public void updateInfo(MemberInfoUpdateRequest updateRequest) {
        validateCanUpdateInfo();
        updateBasicInfo(updateRequest);
        updateMemberDetail(updateRequest);
    }

    // Registration 관련 메서드
    private void initializeBasicInfo(MemberRegisterRequest registerRequest) {
        this.email = new Email(requireNonNull(registerRequest.email()));
        this.nickname = requireNonNull(registerRequest.nickname());
    }

    private void encodePassword(String password, PasswordEncoder passwordEncoder) {
        this.passwordHash = requireNonNull(passwordEncoder.encode(password));
    }

    // Update 관련 메서드
    private void validateCanUpdateInfo() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalArgumentException("활성화된 회원만 정보를 수정할 수 있습니다");
        }
    }

    private void activateMember() {
        this.status = MemberStatus.ACTIVE;
    }

    private void createMemberDetail() {
        this.detail = MemberDetail.create();
    }

    private void updateBasicInfo(MemberInfoUpdateRequest updateRequest) {
        this.nickname = requireNonNull(updateRequest.nickname());
    }

    private void updateMemberDetail(MemberInfoUpdateRequest updateRequest) {
        this.detail.updateInfo(updateRequest);
    }

    // Status 관련 메서드
    private void changeStatusToDeactivated() {
        this.status = MemberStatus.DEACTIVATED;
    }

    private void deactivateMemberDetail() {
        this.detail.deactivate();
    }
}