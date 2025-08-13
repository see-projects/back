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
    @DisplayName("멤버 생성")
    void memberRegister() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDetail().getRegisteredAt()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 일치 여부 확인")
    void verifyPassword() {
        assertThat(member.verifyPassword("longsecret", passwordEncoder)).isTrue();
        assertThat(member.verifyPassword("longsecret1", passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("비밀번호 변경 여부 확인")
    void changePassword() {
        member.changePassword("verysecret", passwordEncoder);

        assertThat(member.verifyPassword("verysecret", passwordEncoder)).isTrue();
    }

    @Test
    @DisplayName("회원 비활성화 여부 확인")
    void deactivate() {
        member.deactivate();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 비활성화 여부 실패 확인")
    void deactivateFail() {
        member.deactivate();

        assertThatThrownBy(() -> member.deactivate())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
