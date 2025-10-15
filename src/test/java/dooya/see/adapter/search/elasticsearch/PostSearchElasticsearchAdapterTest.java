package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.domain.post.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
record PostSearchElasticsearchAdapterTest(
        PostSearchElasticsearchAdapter adapter,
        PostSearchElasticsearchRepository repository) {
    private static final Long AUTHOR_ID = 1L;

    private static Post post;

    @BeforeEach
    void setUp() {
        post = Post.create(createPostRequest(), AUTHOR_ID);
        setField(post, "id", 1L);
    }

    @Test
    void 게시글을_Elasticsearch_색인으로_저장한다() {
        adapter.index(post);

        PostDocument saved = repository.findById(post.getId()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("테스트 게시글 제목입니다");
        assertThat(saved.getTags()).containsExactly("#spring", "#backend", "#java");
    }

    @Test
    void 게시글_색인을_삭제한다() {
        adapter.index(post);
        assertThat(repository.existsById(post.getId())).isTrue();

        adapter.delete(post.getId());

        assertThat(repository.existsById(post.getId())).isFalse();
    }
}
