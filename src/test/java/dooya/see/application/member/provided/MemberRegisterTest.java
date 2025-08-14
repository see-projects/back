package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.*;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberRegisterTest(MemberRegister memberRegister, EntityManager entityManager) {
    @Test
    @DisplayName("회원 등록 시 ID가 부여되고 ACTIVE 상태로 설정된다")
    void register() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("동일한 이메일로 회원 등록 시 중복 예외가 발생한다")
    void duplicateEmailFail() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        assertThatThrownBy(() -> memberRegister.register(createMemberRegisterRequest()))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("회원 비활성화 시 DEACTIVATED 상태와 비활성화일시가 설정된다")
    void deactivate() {
        Member member = registerMember();

        member = memberRegister.deactivate(member.getId());

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 정보 수정 시 닉네임과 프로필 정보가 변경된다")
    void updateInfo() {
        Member member = registerMember();

        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("dooya2", "america", "자기소개"));

        Member updatedMember = entityManager.find(Member.class, member.getId());
        
        assertThat(updatedMember.getNickname()).isEqualTo("dooya2");
        assertThat(updatedMember.getDetail().getProfile().address()).isEqualTo("america");
        assertThat(updatedMember.getDetail().getIntroduction()).isEqualTo("자기소개");
    }

    @Test
    @DisplayName("프로필 주소 중복 검증과 변경 시나리오를 테스트한다")
    void updateInfoFail () {
        Member member = registerMember();
        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("dooya2", "korea", "자기소개"));

        Member member2 = registerMember("dooya1441@see.com");
        entityManager.flush();
        entityManager.clear();

        // member2는 기존의 member와 같은 profile을 사용할 수 없다
        assertThatThrownBy(() -> memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("dooya1441", "korea", "자기소개")))
            .isInstanceOf(DuplicateProfileException.class);

        // 다른 프로필 주소로는 변경 가능
        memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("dooya1441", "japan", "자기소개"));

        // 기존 프로필 주소를 바꾸는 것도 가능
        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("dooya", "china", "자기소개"));

        // 프로필 주소를 제거하는 것도 가능
        memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("dooya1441", "", "자기소개"));
        
        // member가 다시 "china"를 사용하려고 하면 중복 예외 발생
        assertThatThrownBy(() -> memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("dooya1441", "china", "자기소개")))
            .isInstanceOf(DuplicateProfileException.class);
    }

    @Test
    @DisplayName("잘못된 회원 등록 요청 시 검증 예외가 발생한다")
    void memberRegisterRequestFail() {
        checkValidation(new MemberRegisterRequest("valid@email.com", "dooyayayayayaayayayayaya", "longsecret"));
        checkValidation(new MemberRegisterRequest("valid@email.com", "dooya", "secret"));
        checkValidation(new MemberRegisterRequest("valid@email.com", "do", "longsecret"));
    }

    private Member registerMember() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private Member registerMember(String email) {
        Member member = memberRegister.register(createMemberRegisterRequest(email));
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private void checkValidation(MemberRegisterRequest invalid) {
        assertThatThrownBy(() -> memberRegister.register(invalid))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
