package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.post.required.PostSearchReader;
import dooya.see.application.post.dto.PostSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostSearchElasticsearchReaderAdapter implements PostSearchReader {
    private final PostSearchElasticsearchRepository repository;
    private final PostDocumentMapper mapper;

    @Override
    public Page<PostSearchResult> searchByKeyword(String keyword, Pageable pageable) {
        Page<PostDocument> result = repository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        return result.map(mapper::toSearchResult);
    }
}
