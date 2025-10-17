package dooya.see.application.post;

import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "see.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = "${see.kafka.topics.post-events:post-events}")
@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
class PostEventKafkaPipelineTest {

    private final PostManager postManager;
    private final RecordingPostSearchIndexer recordingIndexer;

    PostEventKafkaPipelineTest(PostManager postManager, RecordingPostSearchIndexer recordingIndexer) {
        this.postManager = postManager;
        this.recordingIndexer = recordingIndexer;
    }

    @BeforeEach
    void setUp() {
        recordingIndexer.reset();
    }

    @Test
    void 게시글_생성_이벤트는_Kafka를_통해_색인된다() {
        Post created = postManager.create(createPostRequest(), authorId());

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(recordingIndexer.indexedPosts())
                        .anyMatch(post -> post.getId().equals(created.getId())));
    }

    @Test
    void 게시글_수정_이벤트는_Kafka를_통해_색인이_갱신된다() {
        Post created = postManager.create(createPostRequest(), authorId());
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(recordingIndexer.indexedPosts())
                        .anyMatch(post -> post.getId().equals(created.getId())));

        recordingIndexer.reset();

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                Optional.of("변경된 제목"),
                Optional.of("변경된 본문"),
                Optional.empty(),
                Optional.empty()
        );

        Post updated = postManager.update(updateRequest, created.getId(), authorId());

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(recordingIndexer.indexedPosts())
                        .anyMatch(post -> post.getId().equals(updated.getId())
                                && post.getContent().title().equals("변경된 제목")));
    }

    @Test
    void 게시글_삭제_이벤트는_Kafka를_통해_색인이_삭제된다() {
        Post created = postManager.create(createPostRequest(), authorId());
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(recordingIndexer.indexedPosts())
                        .anyMatch(post -> post.getId().equals(created.getId())));

        recordingIndexer.reset();

        postManager.delete(created.getId(), authorId());

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(recordingIndexer.deletedPostIds()).contains(created.getId()));
    }

    private static Long authorId() {
        return 1L;
    }

    static class RecordingPostSearchIndexer implements PostSearchIndexer {
        private final CopyOnWriteArrayList<Post> indexed = new CopyOnWriteArrayList<>();
        private final CopyOnWriteArrayList<Long> deleted = new CopyOnWriteArrayList<>();

        @Override
        public void index(Post post) {
            indexed.add(post);
        }

        @Override
        public void delete(Long postId) {
            deleted.add(postId);
        }

        List<Post> indexedPosts() {
            return List.copyOf(indexed);
        }

        List<Long> deletedPostIds() {
            return List.copyOf(deleted);
        }

        void reset() {
            indexed.clear();
            deleted.clear();
        }
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration {
        @Bean
        @Primary
        RecordingPostSearchIndexer recordingPostSearchIndexer() {
            return new RecordingPostSearchIndexer();
        }
    }
}
