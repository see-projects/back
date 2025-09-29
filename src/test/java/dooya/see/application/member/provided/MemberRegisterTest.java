package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.*;
import dooya.see.domain.member.dto.MemberInfoUpdateRequest;
import dooya.see.domain.member.exception.DuplicateEmailException;
import dooya.see.domain.member.exception.DuplicateProfileException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 회원_등록 {
        @Test
        void 등록_시_ID가_부여되고_ACTIVE_상태로_설정된다() {
            Member member = memberRegister.register(createMemberRegisterRequest());

            assertThatMemberRegistered(member);
        }

        @Test
        void 동일한_이메일로_등록_시_중복_예외가_발생한다() {
            memberRegister.register(createMemberRegisterRequest());

            assertThatThrownBy(() -> memberRegister.register(createMemberRegisterRequest()))
                    .isInstanceOf(DuplicateEmailException.class);
        }

        private void assertThatMemberRegistered(Member member) {
            assertThat(member.getId()).isNotNull();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }
    }

    @Nested
    class 회원_비활성화 {
        @Test
        void 비활성화_시_DEACTIVATED_상태와_비활성화일시가_설정된다() {
            Member member = registerMemberAndClearContext();

            Member deactivatedMember = memberRegister.deactivate(member.getId());

            assertThatMemberDeactivated(deactivatedMember);
        }

        private void assertThatMemberDeactivated(Member member) {
            assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
            assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
        }
    }

    @Nested
    class 회원_정보_수정 {
        @Test
        void 정보_수정_시_닉네임과_프로필_정보가_변경된다() {
            Member member = registerMemberAndClearContext();
            MemberInfoUpdateRequest updateRequest = new MemberInfoUpdateRequest("dooya2", "america", "자기소개");

            memberRegister.updateInfo(member.getId(), updateRequest);

            assertThatMemberInfoUpdated(member.getId(), updateRequest);
        }

        private void assertThatMemberInfoUpdated(Long memberId, MemberInfoUpdateRequest expected) {
            Member updatedMember = entityManager.find(Member.class, memberId);
            assertThat(updatedMember.getNickname()).isEqualTo(expected.nickname());
            assertThat(updatedMember.getDetail().getProfile().address()).isEqualTo(expected.profileAddress());
            assertThat(updatedMember.getDetail().getIntroduction()).isEqualTo(expected.introduction());
        }
    }

    @Nested
    class 프로필_주소_중복_검증 {
        @Test
        void 중복된_프로필_주소로_변경_시_예외가_발생한다() {
            Member member1 = setupMemberWithProfile("korea");
            Member member2 = registerMemberAndClearContext("dooya1441@see.com");

            assertThatThrownBy(() -> updateMemberProfile(member2.getId(), "korea"))
                    .isInstanceOf(DuplicateProfileException.class);
        }

        @Test
        void 다른_프로필_주소로_변경할_수_있다() {
            Member member1 = setupMemberWithProfile("korea");
            Member member2 = registerMemberAndClearContext("dooya1441@see.com");

            updateMemberProfile(member2.getId(), "japan");

            assertThatProfileUpdated(member2.getId(), "japan");
        }

        @Test
        void 기존_프로필_주소를_변경할_수_있다() {
            Member member = setupMemberWithProfile("korea");

            updateMemberProfile(member.getId(), "china");

            assertThatProfileUpdated(member.getId(), "china");
        }

        @Test
        void 프로필_주소를_제거할_수_있다() {
            Member member1 = setupMemberWithProfile("korea");
            Member member2 = registerMemberAndClearContext("dooya1441@see.com");

            updateMemberProfile(member2.getId(), "");

            assertThatProfileUpdated(member2.getId(), "");
        }

        @Test
        void 프로필_주소_변경_후_다시_중복_시도_시_예외가_발생한다() {
            Member member1 = setupMemberWithProfile("china");
            Member member2 = registerMemberAndClearContext("dooya1441@see.com");

            assertThatThrownBy(() -> updateMemberProfile(member2.getId(), "china"))
                    .isInstanceOf(DuplicateProfileException.class);
        }

        private Member setupMemberWithProfile(String profileAddress) {
            Member member = registerMemberAndClearContext();
            updateMemberProfile(member.getId(), profileAddress);
            return member;
        }

        private void updateMemberProfile(Long memberId, String profileAddress) {
            MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("dooya", profileAddress, "자기소개");
            memberRegister.updateInfo(memberId, request);
            flushAndClearContext();
        }

        private void assertThatProfileUpdated(Long memberId, String expectedProfile) {
            Member updatedMember = entityManager.find(Member.class, memberId);
            assertThat(updatedMember.getDetail().getProfile().address()).isEqualTo(expectedProfile);
        }
    }

    // 헬퍼 메서드들
    private Member registerMemberAndClearContext() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        flushAndClearContext();
        return member;
    }

    private Member registerMemberAndClearContext(String email) {
        Member member = memberRegister.register(createMemberRegisterRequest(email));
        flushAndClearContext();
        return member;
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}