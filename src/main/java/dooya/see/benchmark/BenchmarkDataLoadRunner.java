package dooya.see.benchmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("benchmark-data")
@Component
@RequiredArgsConstructor
public class BenchmarkDataLoadRunner implements CommandLineRunner {
    private final SearchDatasetGenerator datasetGenerator;
    private final BenchmarkDataProperties properties;

    @Override
    public void run(String... args) {
        log.info("Benchmark data profile active. properties={}", properties);
        datasetGenerator.populate(properties);
    }
}
