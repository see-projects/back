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
    void 등록된_회원은_ACTIVE_상태이고_등록일시가_설정되어_있다() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDetail().getRegisteredAt()).isNotNull();
    }

    @Test
    void 올바른_비밀번호와_틀린_비밀번호를_구분하여_검증한다() {
        assertThat(member.verifyPassword("longsecret", passwordEncoder)).isTrue();
        assertThat(member.verifyPassword("longsecret1", passwordEncoder)).isFalse();
    }

    @Test
    void 비밀번호_변경_후_새_비밀번호로만_인증에_성공한다() {
        member.changePassword("verysecret", passwordEncoder);

        assertThat(member.verifyPassword("verysecret", passwordEncoder)).isTrue();
    }

    @Test
    void 회원_비활성화_시_DEACTIVATED_상태와_비활성화일시가_설정된다() {
        member.deactivate();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    void 이미_비활성화된_회원을_다시_비활성화하면_예외가_발생한다() {
        member.deactivate();

        assertThatThrownBy(() -> member.deactivate())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 회원_정보_수정_시_닉네임_프로필_주소_자기소개가_변경된다() {
        var request = new MemberInfoUpdateRequest("dooya", "korea", "자기소개");
        member.updateInfo(request);

        assertThat(member.getNickname()).isEqualTo(request.nickname());
        assertThat(member.getDetail().getProfile().address()).isEqualTo(request.profileAddress());
        assertThat(member.getDetail().getIntroduction()).isEqualTo(request.introduction());
    }
}
