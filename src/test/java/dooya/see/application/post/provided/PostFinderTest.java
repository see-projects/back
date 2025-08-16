package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostFinderTest(PostFinder postFinder, PostManager postManager, EntityManager entityManager) {

}