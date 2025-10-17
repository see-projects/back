package dooya.see.benchmark;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchBenchmarkScenario {
    private final List<String> keywords;
    private final List<Pageable> pageables;
    private final AtomicInteger counter = new AtomicInteger();

    public SearchBenchmarkScenario(List<String> keywords, List<Integer> pageSizes) {
        this.keywords = keywords.isEmpty() ? List.of("Spring") : List.copyOf(keywords);
        this.pageables = pageSizes.isEmpty()
                ? List.of(PageRequest.of(0, 10))
                : pageSizes.stream()
                .map(size -> (Pageable) PageRequest.of(0, size))
                .toList();
    }

    public SearchQuery next() {
        int idx = counter.getAndIncrement();
        String keyword = keywords.get(idx % keywords.size());
        Pageable pageable = pageables.get(idx % pageables.size());
        return new SearchQuery(keyword, pageable);
    }

    public record SearchQuery(String keyword, Pageable pageable) {
    }
}
