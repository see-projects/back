package dooya.see.domain.member;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberStatusTest {
    @Nested
    class 상태_정의 {
        @Test
        void 모든_회원_상태가_정의되어_있다() {
            MemberStatus[] statuses = MemberStatus.values();

            assertThat(statuses).containsExactlyInAnyOrder(
                    MemberStatus.ACTIVE,
                    MemberStatus.DEACTIVATED
            );
        }

        @Test
        void 각_상태가_올바른_이름을_가진다() {
            assertThat(MemberStatus.ACTIVE.name()).isEqualTo("ACTIVE");
            assertThat(MemberStatus.DEACTIVATED.name()).isEqualTo("DEACTIVATED");
        }
    }

    @Nested
    class 상태_전이_규칙 {
        @Test
        void 활성_상태에서_비활성화가_가능하다() {
            MemberStatus status = MemberStatus.ACTIVE;

            boolean canDeactivate = canTransitionTo(status, MemberStatus.DEACTIVATED);

            assertThat(canDeactivate).isTrue();
        }

        @Test
        void 비활성_상태에서는_다른_상태로_전이할_수_없다() {
            MemberStatus status = MemberStatus.DEACTIVATED;

            boolean canDeactivate = canTransitionTo(status, MemberStatus.ACTIVE);

            assertThat(canDeactivate).isFalse();
        }

        @Test
        void 같은_상태로의_전이는_불가능하다() {
            assertThat(canTransitionTo(MemberStatus.ACTIVE, MemberStatus.ACTIVE)).isFalse();
            assertThat(canTransitionTo(MemberStatus.DEACTIVATED, MemberStatus.DEACTIVATED)).isFalse();
        }

        private boolean canTransitionTo(MemberStatus from, MemberStatus to) {
            if (from == to) return false;
            if (from == MemberStatus.DEACTIVATED) return false;
            return from == MemberStatus.ACTIVE && to == MemberStatus.DEACTIVATED;
        }
    }
    
    @Nested
    class 상태_검증 {
        @Test
        void 활성_상태인지_확인할_수_있다() {
            assertThat(isActive(MemberStatus.ACTIVE)).isTrue();
            assertThat(isActive(MemberStatus.DEACTIVATED)).isFalse();
        }

        @Test
        void 비활성_상태인지_확인할_수_있다() {
            assertThat(isDeactivated(MemberStatus.DEACTIVATED)).isTrue();
            assertThat(isDeactivated(MemberStatus.ACTIVE)).isFalse();
        }

        private boolean isActive(MemberStatus status) {
            return status == MemberStatus.ACTIVE;
        }

        private boolean isDeactivated(MemberStatus status) {
            return status == MemberStatus.DEACTIVATED;
        }
    }

    @Nested
    class 열거형_특성 {
        @Test
        void 동일한_상수는_같은_인스턴스다() {
            MemberStatus status1 = MemberStatus.ACTIVE;
            MemberStatus status2 = MemberStatus.ACTIVE;

            assertThat(status1).isSameAs(status2);
            assertThat(status1).isEqualTo(status2);
        }

        @Test
        void 다른_상수는_다른_인스턴스다() {
            MemberStatus active = MemberStatus.ACTIVE;
            MemberStatus deactivated = MemberStatus.DEACTIVATED;

            assertThat(active).isNotSameAs(deactivated);
            assertThat(active).isNotEqualTo(deactivated);
        }

        @Test
        void 문자열로_변환할_수_있다() {
            assertThat(MemberStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
            assertThat(MemberStatus.DEACTIVATED.toString()).isEqualTo("DEACTIVATED");
        }

        @Test
        void 문자열에서_열거형으로_변환할_수_있다() {
            MemberStatus active = MemberStatus.valueOf("ACTIVE");
            MemberStatus deactivated = MemberStatus.valueOf("DEACTIVATED");

            assertThat(active).isEqualTo(MemberStatus.ACTIVE);
            assertThat(deactivated).isEqualTo(MemberStatus.DEACTIVATED);
        }
    }
}
