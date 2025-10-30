package dooya.see.adapter.integration.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.integration.cache.config.PostSearchCacheProperties;
import dooya.see.adapter.integration.cache.key.PostCacheKey;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.application.post.required.PostSearchCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPostSearchCacheRepository implements PostSearchCacheRepository {
    private static final String SEARCH_KEY_PATTERN = "post:search:*";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostSearchCacheProperties properties;

    @Override
    public Optional<Page<PostSearchResult>> find(String keyword, Pageable pageable) {
        String key = buildKey(keyword, pageable);
        String cached = redisTemplate.opsForValue().get(key);

        if (cached == null) {
            return Optional.empty();
        }

        try {
            CachedPage cachedPage = objectMapper.readValue(cached, CachedPage.class);
            return Optional.of(cachedPage.toPage(pageable));
        } catch (JsonProcessingException e) {
            log.warn("검색 캐시 역직렬화 실패: key={}", key, e);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void save(String keyword, Pageable pageable, Page<PostSearchResult> page) {
        String key = buildKey(keyword, pageable);
        CachedPage cachedPage = CachedPage.from(page);

        try {
            String serialized = objectMapper.writeValueAsString(cachedPage);
            Duration ttl = properties.getTtl();
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                redisTemplate.opsForValue().set(key, serialized);
            } else {
                redisTemplate.opsForValue().set(key, serialized, ttl);
            }
        } catch (JsonProcessingException e) {
            log.warn("검색 캐시 직렬화 실패: key={}", key, e);
        }
    }

    @Override
    public void evictAll() {
        Set<String> keys = redisTemplate.keys(SEARCH_KEY_PATTERN);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    private String buildKey(String keyword, Pageable pageable) {
        String normalized = normalize(keyword);
        return PostCacheKey.search(normalized, pageable.getPageNumber(), pageable.getPageSize());
    }

    private String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private record CachedPage(
            List<PostSearchResult> content,
            long totalElements
    ) {
        private Page<PostSearchResult> toPage(Pageable pageable) {
            return new PageImpl<>(content, pageable, totalElements);
        }

        private static CachedPage from(Page<PostSearchResult> page) {
            return new CachedPage(page.getContent(), page.getTotalElements());
        }
    }
}
