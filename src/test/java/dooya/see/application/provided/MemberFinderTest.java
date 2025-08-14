package dooya.see.application.provided;

import dooya.see.SeeTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
public record MemberFinderTest() {
}
