package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostSearchElasticsearchAdapter implements PostSearchIndexer {
    private final PostSearchElasticsearchRepository repository;
    private final PostDocumentMapper mapper;

    @Override
    public void index(Post post) {
        PostDocument document = mapper.toDocument(post);
        repository.save(document);
    }

    @Override
    public void delete(Long postId) {
        repository.deleteById(postId);
    }
}
