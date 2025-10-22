package dooya.see.adapter.integration.cache.key;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostCacheKeyTest {
    @Test
    void 게시글_상세_키는_post_detail_id_형태로_생성된다() {
        assertThat(PostCacheKey.detail(42L)).isEqualTo("post:detail:42");
    }

    @Test
    void 검색_키는_해시값과_페이지_정보를_포함한다() {
        String key = PostCacheKey.search("keyword=hello", 0, 20);
        assertThat(key).startsWith("post:search:");
        assertThat(key).endsWith(":0:20");
    }

    @Test
    void 카테고리_목록_키는_대문자_카테고리와_페이지_정보를_포함한다() {
        String key = PostCacheKey.categoryList("tech", 1, 10);
        assertThat(key).isEqualTo("post:list:category:TECH:1:10");
    }

    @Test
    void 공개_목록_키는_페이지와_사이즈를_그대로_붙인다() {
        assertThat(PostCacheKey.publicList(2, 50)).isEqualTo("post:list:public:2:50");
    }

    @Test
    void 통계_키는_post_stats_id_형태로_생성된다() {
        assertThat(PostCacheKey.stats(7L)).isEqualTo("post:stats:7");
    }

    @Test
    void 음수_ID가_들어오면_예외를_던진다() {
        assertThatThrownBy(() -> PostCacheKey.detail(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 검색어가_null_or_blank이면_empty_해시로_처리한다() {
        assertThat(PostCacheKey.search(null, 0, 10)).contains("post:search:empty:");
        assertThat(PostCacheKey.search("   ", 0, 10)).contains("post:search:empty:");
    }
}
