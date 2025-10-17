package dooya.see.application.post;

import dooya.see.application.post.provided.PostManager;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.dto.PostUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "see.kafka.enabled=false")
class PostEventDirectPipelineTest {

    private final PostManager postManager;
    private final RecordingPostSearchIndexer recordingIndexer;

    PostEventDirectPipelineTest(PostManager postManager, RecordingPostSearchIndexer recordingIndexer) {
        this.postManager = postManager;
        this.recordingIndexer = recordingIndexer;
    }

    @BeforeEach
    void setUp() {
        recordingIndexer.reset();
    }

    @Test
    void Kafka_비활성화_시에도_도메인_이벤트가_즉시_색인된다() {
        Post created = postManager.create(createPostRequest(), authorId());

        assertThat(recordingIndexer.indexedPosts())
                .anyMatch(post -> post.getId().equals(created.getId()));
    }

    @Test
    void Kafka_비활성화_시_수정_이벤트도_즉시_처리된다() {
        Post created = postManager.create(createPostRequest(), authorId());
        recordingIndexer.reset();

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                Optional.of("직접 변경된 제목"),
                Optional.of("직접 변경된 본문"),
                Optional.empty(),
                Optional.empty()
        );

        Post updated = postManager.update(updateRequest, created.getId(), authorId());

        assertThat(recordingIndexer.indexedPosts())
                .anyMatch(post -> post.getId().equals(updated.getId())
                        && post.getContent().title().equals("직접 변경된 제목"));
    }

    @Test
    void Kafka_비활성화_시_삭제_이벤트도_즉시_처리된다() {
        Post created = postManager.create(createPostRequest(), authorId());
        recordingIndexer.reset();

        postManager.delete(created.getId(), authorId());

        assertThat(recordingIndexer.deletedPostIds()).contains(created.getId());
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
