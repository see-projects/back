package dooya.see.adapter.search.elasticsearch;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import dooya.see.domain.post.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
record PostSearchElasticsearchAdapterTest(
        PostSearchElasticsearchAdapter adapter,
        PostSearchElasticsearchRepository repository,
        MemberRepository memberRepository) {
    private static Post post;
    private static Member savedMember;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        MemberRegisterRequest registerRequest = MemberFixture.createMemberRegisterRequest("member" + System.nanoTime() + "@see.com");
        Member member = Member.register(registerRequest, MemberFixture.createPasswordEncoder());
        savedMember = memberRepository.save(member);

        post = Post.create(createPostRequest(), savedMember.getId());
        setField(post, "id", 1L);
    }

    @Test
    void 게시글을_Elasticsearch_색인으로_저장한다() {
        adapter.index(post);

        PostDocument saved = repository.findById(String.valueOf(post.getId())).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("테스트 게시글 제목입니다");
        assertThat(saved.getTags()).containsExactly("spring", "backend", "java");
        assertThat(saved.getAuthorNickname()).isEqualTo(savedMember.getNickname());
    }

    @Test
    void 게시글_색인을_삭제한다() {
        adapter.index(post);
        assertThat(repository.existsById(String.valueOf(post.getId()))).isTrue();

        adapter.delete(post.getId());

        assertThat(repository.existsById(String.valueOf(post.getId()))).isFalse();
    }

    @Test
    void 존재하지_않는_회원이면_작성자_정보가_비어있다() {
        Post orphan = Post.create(createPostRequest(), 999L);
        setField(orphan, "id", 2L);

        adapter.index(orphan);

        PostDocument indexed = repository.findById(String.valueOf(2L)).orElseThrow();
        assertThat(indexed.getAuthorNickname()).isEmpty();
    }
}
