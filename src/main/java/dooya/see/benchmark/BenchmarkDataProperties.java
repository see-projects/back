package dooya.see.benchmark;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "benchmark.data")
public class BenchmarkDataProperties {
    /**
     * 생성할 게시글 수.
     */
    private long postCount = 50_000;

    /**
     * 한 번에 저장할 배치 크기.
     */
    private int batchSize = 500;

    /**
     * 기존 데이터를 무시하고 재생성할지 여부.
     */
    private boolean forceReload = false;

    /**
     * 제목/본문에 사용할 키워드 목록.
     */
    private List<String> keywords = List.of("Spring", "Java", "Kotlin", "Elasticsearch", "Query", "Optimization");

    /**
     * 검색 시나리오에서 사용할 페이지 크기 후보.
     */
    private List<Integer> pageSizes = List.of(10, 20, 50);

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isForceReload() {
        return forceReload;
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<Integer> getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(List<Integer> pageSizes) {
        this.pageSizes = pageSizes;
    }

    @Override
    public String toString() {
        return "BenchmarkDataProperties{" +
                "postCount=" + postCount +
                ", batchSize=" + batchSize +
                ", forceReload=" + forceReload +
                ", keywords=" + keywords +
                ", pageSizes=" + pageSizes +
                '}';
    }
}
