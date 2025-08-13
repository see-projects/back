package dooya.see.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.member.MemberFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {
    Member member;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = createPasswordEncoder();
        member = Member.register(createMemberRegisterRequest(), passwordEncoder);
    }

    @Test
    @DisplayName("등록된 회원은 ACTIVE 상태이고 등록일시가 설정되어 있다")
    void memberRegister() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDetail().getRegisteredAt()).isNotNull();
    }

    @Test
    @DisplayName("올바른 비밀번호와 틀린 비밀번호를 구분하여 검증한다")
    void verifyPassword() {
        assertThat(member.verifyPassword("longsecret", passwordEncoder)).isTrue();
        assertThat(member.verifyPassword("longsecret1", passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("비밀번호 변경 후 새 비밀번호로만 인증에 성공한다")
    void changePassword() {
        member.changePassword("verysecret", passwordEncoder);

        assertThat(member.verifyPassword("verysecret", passwordEncoder)).isTrue();
    }

    @Test
    @DisplayName("회원 비활성화 시 DEACTIVATED 상태와 비활성화일시가 설정된다")
    void deactivate() {
        member.deactivate();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 비활성화된 회원을 다시 비활성화하면 예외가 발생한다")
    void deactivateAlreadyDeactivatedMember() {
        member.deactivate();

        assertThatThrownBy(() -> member.deactivate())
                .isInstanceOf(IllegalStateException.class);
    }
}
