package dooya.see.benchmark;

import dooya.see.SeeApplication;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.dto.PostSearchRequest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 5, time = 5)
@Fork(1)
public class PostSearchBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private ConfigurableApplicationContext context;
        private PostFinder postFinder;
        private SearchBenchmarkScenario scenario;

        @Setup(Level.Trial)
        public void setUp() {
            context = new SpringApplicationBuilder(SeeApplication.class)
                    .profiles("benchmark")
                    .logStartupInfo(false)
                    .run();
            postFinder = context.getBean(PostFinder.class);
            scenario = context.getBean(SearchBenchmarkScenarioProvider.class).createScenario();

            // warm-up search to prime caches
            for (int i = 0; i < 20; i++) {
                SearchBenchmarkScenario.SearchQuery query = scenario.next();
                postFinder.search(buildRequest(query.keyword()), query.pageable());
                postFinder.searchPosts(query.keyword(), query.pageable());
            }
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (context != null) {
                context.close();
            }
        }
    }

    @Benchmark
    public void databaseSearch(BenchmarkState state, Blackhole blackhole) {
        SearchBenchmarkScenario.SearchQuery query = state.scenario.next();
        Page<?> result = state.postFinder.search(buildRequest(query.keyword()), query.pageable());
        blackhole.consume(result.getTotalElements());
    }

    @Benchmark
    public void elasticsearchSearch(BenchmarkState state, Blackhole blackhole) {
        SearchBenchmarkScenario.SearchQuery query = state.scenario.next();
        Page<PostSearchResult> result = state.postFinder.searchPosts(query.keyword(), query.pageable());
        blackhole.consume(result.getTotalElements());
    }

    private static PostSearchRequest buildRequest(String keyword) {
        return PostSearchRequest.builder()
                .keyword(keyword)
                .status(null)
                .build();
    }
}
