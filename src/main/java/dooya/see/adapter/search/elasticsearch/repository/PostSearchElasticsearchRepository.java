package dooya.see.adapter.search.elasticsearch.repository;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchElasticsearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    Page<PostDocument> findByTitleContainingOrContentContaining(String title, String body, Pageable pageable);
}
