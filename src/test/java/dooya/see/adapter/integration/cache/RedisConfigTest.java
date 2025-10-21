package dooya.see.adapter.integration.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public record RedisConfigTest(RedisTemplate<String, String> redisTemplate) {
    @Test
    void RedisTemplate_설정_확인() {
        assertThat(redisTemplate).isNotNull();
    }
}
