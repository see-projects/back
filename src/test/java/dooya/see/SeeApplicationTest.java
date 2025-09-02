package dooya.see;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeeApplicationTest {
    @DisplayName("Spring Boot 애플리케이션이 정상적으로 시작된다")
    @Test
    void contextLoads() {
        // Spring Boot 컨텍스트가 정상적으로 로드되는지 확인
        // @SpringBootTest가 애플리케이션 시작을 검증함
    }
}
