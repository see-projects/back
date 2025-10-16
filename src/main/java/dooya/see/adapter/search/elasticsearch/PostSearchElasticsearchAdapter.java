package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PostSearchElasticsearchAdapter implements PostSearchIndexer {
    private final PostSearchElasticsearchRepository repository;
    private final PostDocumentMapper mapper;
    private final MemberRepository memberRepository;

    @Override
    public void index(Post post) {
        requireNonNull(post, "post must not be null");

        String nickname = memberRepository.findById(post.getMemberId())
                .map(member -> member.getNickname() != null ? member.getNickname() : "")
                .orElseGet(() -> {
                    log.warn("회원 ID={} 을(를) 찾을 수 없어 빈 닉네임으로 색인합니다", post.getMemberId());
                    return "";
                });

        repository.save(mapper.toDocument(post, nickname));
    }

    @Override
    public void delete(Long postId) {
        repository.deleteById(String.valueOf(postId));
    }
}
