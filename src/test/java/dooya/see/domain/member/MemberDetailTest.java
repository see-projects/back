package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDetailTest {
    @Test
    void create_메서드로_생성된_MemberDetail은_등록일시가_설정된다() {
        MemberDetail memberDetail = MemberDetail.create();

        assertThat(memberDetail).isNotNull();
        assertThat(memberDetail.getRegisteredAt()).isNotNull();
    }

    @Test
    void 기본_생성자로_생성된_MemberDetail은_등록일시가_null이다() {
        MemberDetail memberDetail = new MemberDetail();

        assertThat(memberDetail).isNotNull();
        assertThat(memberDetail.getRegisteredAt()).isNull();
    }
}
