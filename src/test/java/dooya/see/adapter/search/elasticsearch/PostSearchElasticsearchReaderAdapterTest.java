package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.PostCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record PostSearchElasticsearchReaderAdapterTest(
        PostSearchElasticsearchRepository repository,
        PostSearchElasticsearchReaderAdapter reader) {

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.saveAll(List.of(
                PostDocument.builder()
                        .id(String.valueOf(1L))
                        .title("Spring Elasticsearch 적용기")
                        .content("Spring Boot 프로젝트에서 Elasticsearch 적용 경험기입니다.")
                        .category(PostCategory.TECH.name())
                        .memberId(1L)
                        .authorNickname("작성자1")
                        .tags(List.of("Spring", "Elasticsearch"))
                        .createdAt(LocalDate.now())
                        .build(),
                PostDocument.builder()
                        .id(String.valueOf(2L))
                        .title("Java Concurrency 기초")
                        .content("스레드와 동시성 제어의 기본을 다룹니다.")
                        .category(PostCategory.TECH.name())
                        .memberId(2L)
                        .authorNickname("작성자2")
                        .tags(List.of("Java", "Thread"))
                        .createdAt(LocalDate.now())
                        .build()
        ));
    }

    @Test
    void 제목과_내용을_기준으로_게시글을_검색한다() {
        String keyword = "Spring";
        PageRequest pageable = PageRequest.of(0, 10);

        Page<PostSearchResult> result = reader.searchByKeyword(keyword, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).title()).contains("Spring");
        assertThat(result.getContent().get(0).category()).isEqualTo(PostCategory.TECH);
        assertThat(result.getContent().get(0).authorNickname()).isEqualTo("작성자1");
    }
}
