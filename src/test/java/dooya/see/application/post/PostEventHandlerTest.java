package dooya.see.application.post;

import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.Post;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
record PostEventHandlerTest(
        PostManager postManager,
        PostSearchElasticsearchRepository searchRepository) {
    private static final Long AUTHOR_ID = 1L;

    @Test
    void 게시글_생성시_자동으로_색인된다() {
        Post post = postManager.create(createPostRequest(), AUTHOR_ID);

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertThat(searchRepository.findById(String.valueOf(post.getId()))).isPresent());
    }
}