package dooya.see.application.post.required;

import dooya.see.application.post.dto.PostSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 게시글 검색 결과 캐싱을 위한 포트입니다.
 */
public interface PostSearchCacheRepository {
    /**
     * 캐시에 저장된 검색 결과를 조회합니다.
     *
     * @param keyword  검색 키워드
     * @param pageable 페이징 정보
     * @return 캐시된 검색 결과 페이지
     */
    Optional<Page<PostSearchResult>> find(String keyword, Pageable pageable);

    /**
     * 검색 결과를 캐시에 저장합니다.
     *
     * @param keyword  검색 키워드
     * @param pageable 페이징 정보
     * @param page     저장할 검색 결과 페이지
     */
    void save(String keyword, Pageable pageable, Page<PostSearchResult> page);

    /**
     * 검색 캐시 전체를 삭제합니다.
     */
    void evictAll();
}
