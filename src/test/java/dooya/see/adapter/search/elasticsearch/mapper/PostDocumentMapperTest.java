package dooya.see.adapter.search.elasticsearch.mapper;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostDocumentMapperTest {
    private PostDocumentMapper mapper;
    private Post post;

    @BeforeEach
    void setUp() {
        mapper = new PostDocumentMapper();
        post = Post.create(createPostRequest(), 1L);
        ReflectionTestUtils.setField(post, "id", 10L);
    }

    @Test
    void toDocument는_Post를_PostDocument로_변환한다() {
        PostDocument document = mapper.toDocument(post, "author");

        assertThat(document.getId()).isEqualTo("10");
        assertThat(document.getTitle()).isEqualTo(post.getContent().title());
        assertThat(document.getAuthorNickname()).isEqualTo("author");
        assertThat(document.getTags()).containsExactly("spring", "backend", "java");
    }

    @Test
    void toDocument는_null_Post면_예외를던진다() {
        assertThatThrownBy(() -> mapper.toDocument(null, "author"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("게시글 정보는 필수입니다");
    }

    @Test
    void toDocument는_null_작성자면_예외를던진다() {
        assertThatThrownBy(() -> mapper.toDocument(post, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("작성자 닉네임은 필수입니다");
    }

    @Test
    void toSearchResult는_PostDocument를_PostSearchResult로_변환한다() {
        PostDocument document = PostDocument.builder()
                .id("11")
                .title("테스트")
                .content("본문")
                .category(PostCategory.TECH.name())
                .memberId(2L)
                .authorNickname("작성자")
                .tags(List.of("spring"))
                .createdAt(LocalDate.now())
                .build();

        PostSearchResult result = mapper.toSearchResult(document);

        assertThat(result.id()).isEqualTo(11L);
        assertThat(result.category()).isEqualTo(PostCategory.TECH);
        assertThat(result.tags()).containsExactly("spring");
    }

    @Test
    void toSearchResult는_Id가_숫자가_아니면_예외를던진다() {
        PostDocument document = PostDocument.builder()
                .id("abc")
                .title("테스트")
                .content("본문")
                .category(PostCategory.TECH.name())
                .memberId(2L)
                .authorNickname("작성자")
                .createdAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> mapper.toSearchResult(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검색 문서 ID는 숫자여야 합니다");
    }

    @Test
    void toSearchResult는_null_Document면_예외를던진다() {
        assertThatThrownBy(() -> mapper.toSearchResult(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("검색 문서는 필수입니다");
    }
}
