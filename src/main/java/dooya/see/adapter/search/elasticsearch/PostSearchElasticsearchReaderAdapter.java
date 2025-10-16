package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.post.required.PostSearchReader;
import dooya.see.domain.post.Post;
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
    public Page<Post> searchByKeyword(String keyword, Pageable pageable) {
        Page<PostDocument> result = repository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        return result.map(mapper::toDomain);
    }
}
