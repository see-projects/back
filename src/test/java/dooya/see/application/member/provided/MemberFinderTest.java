package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberFinderTest(MemberFinder memberFinder, MemberRegister memberRegister, EntityManager entityManager) {
    @Test
    @DisplayName("ID로 회원을 조회할 수 있다")
    void find() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        Member found = memberFinder.find(member.getId());

        assertThat(member.getId()).isEqualTo(found.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void findFail() {
        assertThatThrownBy(() -> memberFinder.find(999L))
            .isInstanceOf(MemberNotFoundException.class);
    }
}
