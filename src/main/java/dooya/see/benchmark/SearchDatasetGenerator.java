package dooya.see.benchmark;

import dooya.see.adapter.search.elasticsearch.mapper.PostDocumentMapper;
import dooya.see.adapter.search.elasticsearch.repository.PostSearchElasticsearchRepository;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.PasswordEncoder;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.shared.Email;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchDatasetGenerator {
    private final PostRepository postRepository;
    private final PostSearchElasticsearchRepository postSearchRepository;
    private final PostDocumentMapper postDocumentMapper;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void populate(BenchmarkDataProperties properties) {
        long existing = postRepository.count();
        if (!properties.isForceReload() && existing >= properties.getPostCount()) {
            log.info("Skip dataset generation. existingPostCount={} requestedPostCount={}", existing, properties.getPostCount());
            return;
        }

        log.info("Preparing dataset: deleting existing search index");
        postSearchRepository.deleteAll();

        if (properties.isForceReload()) {
            log.info("Force reload enabled. Removing existing relational data");
            entityManager.createQuery("delete from Post").executeUpdate();
            entityManager.flush();
            entityManager.clear();
        }

        Member writer = ensureBenchmarkMember();

        long targetCount = properties.getPostCount();
        int batchSize = Math.max(1, properties.getBatchSize());
        List<String> keywords = properties.getKeywords();

        List<Post> persisted = new ArrayList<>(batchSize);

        for (long i = 0; i < targetCount; i++) {
            String keyword = keywords.get((int) (i % keywords.size()));
            PostCreateRequest request = new PostCreateRequest(
                    keyword + " 학습 노트 " + i,
                    buildBody(keyword),
                    pickCategory(keyword, i),
                    true,
                    List.of(
                            normalizeTag(keyword),
                            "benchmark",
                            pickRandomTag()
                    )
            );

            Post post = Post.create(request, writer.getId());
            post = postRepository.save(post);
            persisted.add(post);

            if (persisted.size() == batchSize) {
                flushToSearchIndex(writer.getNickname(), persisted);
                entityManager.flush();
                entityManager.clear();
                persisted.clear();
            }

            if ((i + 1) % 5_000 == 0) {
                log.info("Generated {} / {} posts", (i + 1), targetCount);
            }
        }

        if (!persisted.isEmpty()) {
            flushToSearchIndex(writer.getNickname(), persisted);
            entityManager.flush();
            entityManager.clear();
        }

        log.info("Dataset generation completed. totalPosts={}", targetCount);
    }

    private Member ensureBenchmarkMember() {
        String email = "benchmark@see.dooya";
        return memberRepository.findByEmail(new Email(email))
                .orElseGet(() -> {
                    MemberRegisterRequest request = new MemberRegisterRequest(email, "benchmark", "benchmark_pass");
                    Member member = Member.register(request, passwordEncoder);
                    return memberRepository.save(member);
                });
    }

    private void flushToSearchIndex(String nickname, List<Post> posts) {
        postSearchRepository.saveAll(
                posts.stream()
                        .map(post -> postDocumentMapper.toDocument(post, nickname))
                .toList()
        );
    }

    private static String normalizeTag(String keyword) {
        return keyword.replaceAll("[^가-힣a-zA-Z0-9]", "").toLowerCase();
    }

    private static String buildBody(String keyword) {
        return "%s 관련 벤치마크 게시글입니다.".formatted(keyword) +
                " 다양한 검색 조건과 성능 비교를 위한 샘플 데이터를 포함합니다.";
    }

    private static PostCategory pickCategory(String keyword, long sequence) {
        int selector = (keyword.hashCode() ^ Long.hashCode(sequence)) & 0b11;
        return switch (selector) {
            case 0 -> PostCategory.TECH;
            case 1 -> PostCategory.GENERAL;
            case 2 -> PostCategory.QNA;
            default -> PostCategory.NOTICE;
        };
    }

    private static String pickRandomTag() {
        String[] pool = {"springboot", "data", "es", "query", "analysis", "benchmark"};
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }
}
