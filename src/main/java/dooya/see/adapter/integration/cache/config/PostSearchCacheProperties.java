package dooya.see.adapter.integration.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "see.cache.post-search")
public class PostSearchCacheProperties {
    /**
     * 검색 결과 캐시 TTL.
     */
    private Duration ttl = Duration.ofMinutes(3);

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
