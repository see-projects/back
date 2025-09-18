package dooya.see.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dooya.see.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {
    private Member member;
    private PasswordEncoder passwordEncoder;

    private static final String ORIGINAL_PASSWORD = "longsecret";
    private static final String NEW_PASSWORD = "verysecret";
    private static final String WRONG_PASSWORD = "wrong";

    @BeforeEach
    void setUp() {
        this.passwordEncoder = createPasswordEncoder();
        member = Member.register(createMemberRegisterRequest(), passwordEncoder);
    }

    @Nested
    class 회원_등록 {
        @Test
        void 등록된_회원은_활성_상태이다() {
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        void 등록된_회원은_등록일시가_설정된다() {
            assertThat(member.getDetail().getRegisteredAt()).isNotNull();
        }

    }
    @Nested
    class 비밀번호_관리 {
        @Test
        void 올바른_비밀번호로_인증에_성공한다() {
            boolean result = member.verifyPassword(ORIGINAL_PASSWORD, passwordEncoder);

            assertThat(result).isTrue();
        }

        @Test
        void 틀린_비밀번호로_인증에_실패한다() {
            boolean result = member.verifyPassword(WRONG_PASSWORD, passwordEncoder);

            assertThat(result).isFalse();
        }

        @Test
        void 비밀번호_변경_후_새_비밀번호로_인증한다() {
            member.changePassword(NEW_PASSWORD, passwordEncoder);

            assertThatPasswordChanged();
        }

        @Test
        void null_비밀번호_검증_시_예외가_발생한다() {
            assertThatThrownBy(() -> member.verifyPassword(null, passwordEncoder))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 빈_비밀번호로_인증에_실패한다() {
            boolean result = member.verifyPassword("", passwordEncoder);

            assertThat(result).isFalse();
        }

        private void assertThatPasswordChanged() {
            assertThat(member.verifyPassword(NEW_PASSWORD, passwordEncoder)).isTrue();
            assertThat(member.verifyPassword(ORIGINAL_PASSWORD, passwordEncoder)).isFalse();
        }
    }

    @Nested
    class 회원_정보_수정 {
        @Test
        void 회원_정보_수정_시_모든_필드가_변경된다() {
            var updateRequest = createMemberInfoUpdateRequest();

            member.updateInfo(updateRequest);

            assertThatMemberInfoUpdated(updateRequest);
        }

        @Test
        void 비활성화된_회원의_정보_수정_시_예외가_발생한다() {
            member.deactivate();

            assertThatThrownBy(() -> member.deactivate())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private void assertThatMemberInfoUpdated(MemberInfoUpdateRequest request) {
            assertThat(member.getNickname()).isEqualTo(request.nickname());
            assertThat(member.getDetail().getProfile().address()).isEqualTo(request.profileAddress());
            assertThat(member.getDetail().getIntroduction()).isEqualTo(request.introduction());
        }
    }
}
