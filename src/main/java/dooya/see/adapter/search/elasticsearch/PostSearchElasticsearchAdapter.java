package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.member.Member;
import dooya.see.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostSearchElasticsearchAdapter implements PostSearchIndexer {
    private final PostSearchElasticsearchRepository repository;
    private final PostDocumentMapper mapper;
    private final MemberRepository memberRepository;

    @Override
    public void index(Post post) {
        String nickname = memberRepository.findById(post.getMemberId())
                .map(Member::getNickname)
                .orElse("");

        PostDocument document = mapper.toDocument(post, nickname);
        repository.save(document);
    }

    @Override
    public void delete(Long postId) {
        repository.deleteById(postId);
    }
}
