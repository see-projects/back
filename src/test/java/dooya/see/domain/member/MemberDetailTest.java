package dooya.see.domain.member;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberDetailTest {
    @Nested
    class 생성 {
        @Test
        void 정적_팩토리_메서드로_생성_시_등록일시가_설정된다() {
            MemberDetail memberDetail = MemberDetail.create();

            assertThatMemberDetailCreated(memberDetail);
        }

        @Test
        void 기본_생성자로_생성_시_모든_필드가_초기_상태다() {
            MemberDetail memberDetail = new MemberDetail();

            assertThatMemberDetailInitialized(memberDetail);
        }

        private void assertThatMemberDetailCreated(MemberDetail memberDetail) {
            assertThat(memberDetail).isNotNull();
            assertThat(memberDetail.getRegisteredAt()).isNotNull();
            assertThat(memberDetail.getRegisteredAt()).isBefore(LocalDateTime.now().plusSeconds(1));
            assertThat(memberDetail.getDeactivatedAt()).isNull();
        }

        private void assertThatMemberDetailInitialized(MemberDetail memberDetail) {
            assertThat(memberDetail).isNotNull();
            assertThat(memberDetail.getRegisteredAt()).isNull();
            assertThat(memberDetail.getDeactivatedAt()).isNull();
            assertThat(memberDetail.getProfile()).isNull();
            assertThat(memberDetail.getIntroduction()).isNull();
        }
    }

    @Nested
    class 비활성화 {
        @Test
        void 활성화된_회원을_비활성화하면_비활성화일시가_설정된다() {
            MemberDetail memberDetail = MemberDetail.create();

            memberDetail.deactivate();

            assertThatMemberDetailDeactivated(memberDetail);
        }

        @Test
        void 비활성화된_회원을_다시_비활성화하면_예외가_발생한다() {
            MemberDetail memberDetail = MemberDetail.create();
            memberDetail.deactivate();

            assertThatThrownBy(memberDetail::deactivate)
                .isInstanceOf(IllegalArgumentException.class);
        }

        private void assertThatMemberDetailDeactivated(MemberDetail memberDetail) {
            assertThat(memberDetail.getDeactivatedAt()).isNotNull();
            assertThat(memberDetail.getDeactivatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        }
    }
}
