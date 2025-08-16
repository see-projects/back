package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostManagerTest(PostManager postManager) {
    @Test
    @DisplayName("")
    void a() {
        Post post = postManager.create(createPostRequest(), 1L);

        assertThat(post.getId()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData()).isNotNull();
    }
}
