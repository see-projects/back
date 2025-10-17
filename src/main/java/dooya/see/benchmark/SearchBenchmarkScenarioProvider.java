package dooya.see.benchmark;

import org.springframework.stereotype.Component;

@Component
public class SearchBenchmarkScenarioProvider {
    private final BenchmarkDataProperties properties;

    public SearchBenchmarkScenarioProvider(BenchmarkDataProperties properties) {
        this.properties = properties;
    }

    public SearchBenchmarkScenario createScenario() {
        return new SearchBenchmarkScenario(properties.getKeywords(), properties.getPageSizes());
    }
}
