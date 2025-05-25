package dooya.see;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeeApplication.class, args);
    }

}
