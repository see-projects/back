package dooya.see.application.post;

import dooya.see.application.post.provided.PostStatsManager;
import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostStatsService implements PostStatsManager {
    private final PostStatsRepository postStatsRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializePostStats(Long postId) {
        log.debug("게시물 통계 초기화: postId={}", postId);

        if (preventDuplicateStatsCreation(postId))
            return;

        PostStats stats = createNewStats(postId);
        saveStats(stats);
        entityManager.flush();

        log.info("게시물 통계 초기화 완료: postId={}", postId);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long postId) {
        log.debug("조회수 증가 처리: postId={}", postId);
        executeStatsOperation(postId, "조회수 증가", PostStats::incrementViewCount);
    }

    @Override
    @Transactional
    public void incrementLikeCount(Long postId) {
        log.debug("좋아요 수 증가 처리: postId={}", postId);
        executeStatsOperation(postId, "좋아요 증가", PostStats::incrementLikeCount);
    }

    @Override
    @Transactional
    public void decrementLikeCount(Long postId) {
        log.debug("좋아요 수 감소 처리: postId={}", postId);
        executeStatsOperation(postId, "좋아요 감소", PostStats::decrementLikeCount);
    }

    @Override
    @Transactional
    public void incrementCommentCount(Long postId) {
        log.debug("댓글 수 증가 처리: postId={}", postId);
        executeStatsOperation(postId, "댓글 수 증가", PostStats::incrementCommentCount);
    }

    @Override
    @Transactional
    public void decrementCommentCount(Long postId) {
        log.debug("댓글 수 감소 처리: postId={}", postId);
        executeStatsOperation(postId, "댓글 수 감소", PostStats::decrementCommentCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostStats> getPostStats(Long postId) {
        log.debug("게시물 통계 조회: postId={}", postId);
        return findStatsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostStats getPostStatsOrThrow(Long postId) {
        log.debug("게시물 통계 조회(필수): postId={}", postId);
        return findStatsByPostId(postId)
                .orElseThrow(() -> createStatsNotFoundException(postId));
    }

    // Stats Creation 관련 메서드
    private static PostStats createNewStats(Long postId) {
        return PostStats.create(postId);
    }

    private PostStats saveStats(PostStats stats) {
        return postStatsRepository.save(stats);
    }

    // Stats Operation 관련 메서드
    private void executeStatsOperation(Long postId, String operation, StatsOperation statsOperation) {
        Optional<PostStats> statsOptional = findStatsByPostId(postId);

        if (statsOptional.isPresent()) {
            PostStats stats = statsOptional.get();
            statsOperation.execute(stats);
            saveStats(stats);
            logOperationSuccess(operation, postId);
        } else {
            logOperationIgnored(operation, postId);
        }
    }

    // Stats 조회 관련 메서드
    private Optional<PostStats> findStatsByPostId(Long postId) {
        return postStatsRepository.findByPostId(postId);
    }

    // 중복 방지 관련 메서드
    private boolean preventDuplicateStatsCreation(Long postId) {
        if (isStatsAlreadyExists(postId)) {
            logDuplicateStatsWarning(postId);
            return true;
        }
        return false;
    }

    // 검증 관련 메서드
    private boolean isStatsAlreadyExists(Long postId) {
        return postStatsRepository.findByPostId(postId).isPresent();
    }

    // 예외 생성 관련 메서드
    private static IllegalArgumentException createStatsNotFoundException(Long postId) {
        return new IllegalArgumentException("PostStats not found for postId: " + postId);
    }

    // 로깅 관련 메서드
    private void logOperationSuccess(String operation, Long postId) {
        log.info("{} 완료: postId={}", operation, postId);
    }

    private void logOperationIgnored(String operation, Long postId) {
        log.warn("PostStats not found for postId: {}, {} 작업 무시", postId, operation);
    }

    private void logDuplicateStatsWarning(Long postId) {
        log.warn("PostStats already exists for postId: {}", postId);
    }

    // Functional Interface 정의
    @FunctionalInterface
    private interface StatsOperation {
        void execute(PostStats stats);
    }
}
