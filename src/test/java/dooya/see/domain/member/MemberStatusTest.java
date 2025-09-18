package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberStatusTest {
    @Test
    void MemberStatus_enum의_ACTIVE와_DEACTIVATED_상태가_정의되어_있다() {
        assertThat(MemberStatus.ACTIVE).isNotNull();
        assertThat(MemberStatus.DEACTIVATED).isNotNull();
    }
}
