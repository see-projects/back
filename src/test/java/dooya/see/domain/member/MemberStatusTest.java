package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberStatusTest {
    @DisplayName("MemberStatus enum의 ACTIVE와 DEACTIVATED 상태가 정의되어 있다")
    @Test
    void memberStatus_hasRequiredStatuses() {
        assertThat(MemberStatus.ACTIVE).isNotNull();
        assertThat(MemberStatus.DEACTIVATED).isNotNull();
    }
}
