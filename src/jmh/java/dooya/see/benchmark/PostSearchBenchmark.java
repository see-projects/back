package dooya.see.benchmark;

import dooya.see.SeeApplication;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.required.PostSearchCacheRepository;
import dooya.see.domain.post.dto.PostSearchRequest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 5, time = 5)
@Fork(1)
public class PostSearchBenchmark {

    private abstract static class AbstractBenchmarkState {
        protected ConfigurableApplicationContext context;
        protected PostFinder postFinder;
        protected PostSearchCacheRepository cacheRepository;
        protected SearchBenchmarkScenario scenario;
        protected SearchBenchmarkScenario.SearchQuery cacheHitQuery;

        @Setup(Level.Trial)
        public void setUp() {
            context = new SpringApplicationBuilder(SeeApplication.class)
                    .profiles("benchmark", "benchmark-data")
                    .web(WebApplicationType.NONE)
                    .logStartupInfo(false)
                    .run();
            postFinder = context.getBean(PostFinder.class);
            cacheRepository = context.getBean(PostSearchCacheRepository.class);
            scenario = context.getBean(SearchBenchmarkScenarioProvider.class).createScenario();

            cacheHitQuery = scenario.next();
            // warm up DB and cache flows with the hot keyword
            postFinder.search(buildRequest(cacheHitQuery.keyword()), cacheHitQuery.pageable());
            postFinder.searchPosts(cacheHitQuery.keyword(), cacheHitQuery.pageable());
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (context != null) {
                context.close();
            }
        }

        protected SearchBenchmarkScenario.SearchQuery nextQuery() {
            return scenario.next();
        }

        protected SearchBenchmarkScenario.SearchQuery cacheHitQuery() {
            return cacheHitQuery;
        }

        protected static PostSearchRequest buildRequest(String keyword) {
            return PostSearchRequest.builder()
                    .keyword(keyword)
                    .status(null)
                    .build();
        }
    }

    @State(Scope.Benchmark)
    public static class DatabaseSearchState extends AbstractBenchmarkState {
        // inherits base set-up
    }

    @State(Scope.Benchmark)
    public static class ColdSearchState extends AbstractBenchmarkState {
        @Setup(Level.Invocation)
        public void clearCache() {
            cacheRepository.evictAll();
        }
    }

    @State(Scope.Benchmark)
    public static class CachedSearchState extends AbstractBenchmarkState {
        // inherits warm-up behaviour
    }

    @Benchmark
    public void elasticsearchCold(ColdSearchState state, Blackhole blackhole) {
        SearchBenchmarkScenario.SearchQuery query = state.nextQuery();
        Page<PostSearchResult> result = state.postFinder.searchPosts(query.keyword(), query.pageable());
        blackhole.consume(result.getTotalElements());
    }

    @Benchmark
    public void elasticsearchCached(CachedSearchState state, Blackhole blackhole) {
        SearchBenchmarkScenario.SearchQuery query = state.cacheHitQuery();
        Page<PostSearchResult> result = state.postFinder.searchPosts(query.keyword(), query.pageable());
        blackhole.consume(result.getTotalElements());
    }

    @Benchmark
    public void databaseSearch(DatabaseSearchState state, Blackhole blackhole) {
        SearchBenchmarkScenario.SearchQuery query = state.nextQuery();
        Page<?> result = state.postFinder.search(
                AbstractBenchmarkState.buildRequest(query.keyword()),
                query.pageable()
        );
        blackhole.consume(result.getTotalElements());
    }
}
