package dooya.see;

import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.PasswordEncoder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration
public class SeeTestConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return MemberFixture.createPasswordEncoder();
    }

    @Bean(name = "applicationTaskExecutor")
    public Executor synchronousTaskExecutor() {
        return new SyncTaskExecutor();
    }
}
