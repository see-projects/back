package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberStatusTest {
    @Test
    @DisplayName("기본 상태 테스트")
    void memberStatusDefault() {
        assertThat(MemberStatus.ACTIVE).isNotNull();
        assertThat(MemberStatus.DEACTIVATED).isNotNull();
    }
}
