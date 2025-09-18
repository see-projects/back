package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.*;
import dooya.see.domain.member.exception.DuplicateEmailException;
import dooya.see.domain.member.exception.DuplicateProfileException;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
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
    void 회원_등록_시_ID가_부여되고_ACTIVE_상태로_설정된다() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void 동일한_이메일로_회원_등록_시_중복_예외가_발생한다() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        assertThatThrownBy(() -> memberRegister.register(createMemberRegisterRequest()))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 회원_비활성화_시_DEACTIVATED_상태와_비활성화일시가_설정된다() {
        Member member = registerMember();

        member = memberRegister.deactivate(member.getId());

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    void 회원_정보_수정_시_닉네임과_프로필_정보가_변경된다() {
        Member member = registerMember();

        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("dooya2", "america", "자기소개"));

        Member updatedMember = entityManager.find(Member.class, member.getId());
        
        assertThat(updatedMember.getNickname()).isEqualTo("dooya2");
        assertThat(updatedMember.getDetail().getProfile().address()).isEqualTo("america");
        assertThat(updatedMember.getDetail().getIntroduction()).isEqualTo("자기소개");
    }

    @Test
    void 프로필_주소_중복_검증과_변경_시나리오를_테스트한다() {
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
