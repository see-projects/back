package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDetailTest {
    @DisplayName("create 메서드로 생성된 MemberDetail은 등록일시가 설정된다")
    @Test
    void memberDetailCreate() {
        MemberDetail memberDetail = MemberDetail.create();

        assertThat(memberDetail).isNotNull();
        assertThat(memberDetail.getRegisteredAt()).isNotNull();
    }

    @DisplayName("기본 생성자로 생성된 MemberDetail은 등록일시가 null이다")
    @Test
    void memberDetailDefaultConstructor() {
        MemberDetail memberDetail = new MemberDetail();

        assertThat(memberDetail).isNotNull();
        assertThat(memberDetail.getRegisteredAt()).isNull();
    }
}
