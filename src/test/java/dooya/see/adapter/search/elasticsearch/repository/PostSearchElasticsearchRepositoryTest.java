package dooya.see.adapter.search.elasticsearch.repository;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record PostSearchElasticsearchRepositoryTest(PostSearchElasticsearchRepository repository) {
    @Test
    void ElasticSearch_색인_저장_및_조회_테스트() {
        for (int i = 1; i <= 30; i++) {
            PostDocument document = new PostDocument(
                    String.valueOf((long) i),
                    "테스트 제목 " + i,
                    "본문 내용 " + i,
                    "TECH",
                    1L,
                    List.of("spring", "elasticsearch"),
                    LocalDate.now()
            );
            repository.save(document);
        }

        Pageable pageable = PageRequest.of(0, 10);

        Page<PostDocument> result =
                repository.findByTitleContainingOrContentContaining("테스트", "테스트", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isGreaterThan(10);
        assertThat(result.getContent().get(0).getTitle()).contains("테스트");
    }
}
