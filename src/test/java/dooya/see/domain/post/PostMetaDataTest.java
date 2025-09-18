package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMetaDataTest {
    @Nested
    class 메타데이터_생성 {
        @Test
        void 생성_시_생성일시만_설정되고_나머지는_초기값이다() {
            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

            PostMetaData metaData = PostMetaData.create();

            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

            assertThatCreatedWithValidTimestamp(metaData, beforeCreate, afterCreate);
            assertThatOtherFieldsAreInitial(metaData);
        }

        @Test
        void 여러_번_생성해도_각각_다른_생성일시를_가진다() {
            PostMetaData first = PostMetaData.create();

            // 시간차를 만들기 위한 약간의 지연
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PostMetaData second = PostMetaData.create();

            assertThat(second.createdAt()).isAfter(first.createdAt());
        }

        private void assertThatCreatedWithValidTimestamp(PostMetaData metaData, LocalDateTime beforeCreate, LocalDateTime afterCreate) {
            assertThat(metaData.createdAt()).isNotNull();
            assertThat(metaData.createdAt()).isBetween(beforeCreate, afterCreate);
        }

        private void assertThatOtherFieldsAreInitial(PostMetaData metaData) {
            assertThat(metaData.modifiedAt()).isNull();
            assertThat(metaData.publishedAt()).isNull();
        }
    }

    @Nested
    class 수정일시_업데이트 {
        @Test
        void 수정일시만_현재_시간으로_변경되고_나머지는_기존_값을_유지한다() {
            PostMetaData original = PostMetaData.create();
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

            PostMetaData updated = original.updateModifiedAt();

            LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

            assertThatOnlyModifiedAtUpdated(original, updated, beforeUpdate, afterUpdate);
        }

        @Test
        void 여러_번_수정하면_수정일시가_계속_갱신된다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData firstUpdate = original.updateModifiedAt();

            // 시간차를 만들기 위한 지연
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PostMetaData secondUpdate = firstUpdate.updateModifiedAt();

            assertThat(secondUpdate.modifiedAt()).isAfter(firstUpdate.modifiedAt());
            assertThat(secondUpdate.createdAt()).isEqualTo(original.createdAt());
        }

        @Test
        void 발행된_게시글의_수정일시_업데이트_시_발행일시는_유지된다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData published = original.updatePublishedAt();

            PostMetaData modified = published.updateModifiedAt();

            assertThat(modified.publishedAt()).isEqualTo(published.publishedAt());
            assertThat(modified.modifiedAt()).isNotNull();
            assertThat(modified.createdAt()).isEqualTo(original.createdAt());
        }

        private void assertThatOnlyModifiedAtUpdated(PostMetaData original, PostMetaData updated,
                                                     LocalDateTime beforeUpdate, LocalDateTime afterUpdate) {
            assertThat(updated.createdAt()).isEqualTo(original.createdAt());
            assertThat(updated.modifiedAt()).isNotNull();
            assertThat(updated.modifiedAt()).isBetween(beforeUpdate, afterUpdate);
            assertThat(updated.publishedAt()).isEqualTo(original.publishedAt());
        }
    }

    @Nested
    class 발행일시_업데이트 {
        @Test
        void 발행일시만_현재_시간으로_변경되고_나머지는_기존_값을_유지한다() {
            PostMetaData original = PostMetaData.create();
            LocalDateTime beforePublish = LocalDateTime.now().minusSeconds(1);

            PostMetaData published = original.updatePublishedAt();

            LocalDateTime afterPublish = LocalDateTime.now().plusSeconds(1);

            assertThatOnlyPublishedAtUpdated(original, published, beforePublish, afterPublish);
        }

        @Test
        void 재발행_시_발행일시가_갱신된다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData firstPublish = original.updatePublishedAt();

            // 시간차를 만들기 위한 지연
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PostMetaData republish = firstPublish.updatePublishedAt();

            assertThat(republish.publishedAt()).isAfter(firstPublish.publishedAt());
            assertThat(republish.createdAt()).isEqualTo(original.createdAt());
        }

        @Test
        void 수정된_게시글의_발행일시_업데이트_시_수정일시는_유지된다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData modified = original.updateModifiedAt();

            PostMetaData published = modified.updatePublishedAt();

            assertThat(published.modifiedAt()).isEqualTo(modified.modifiedAt());
            assertThat(published.publishedAt()).isNotNull();
            assertThat(published.createdAt()).isEqualTo(original.createdAt());
        }

        private void assertThatOnlyPublishedAtUpdated(PostMetaData original, PostMetaData published,
                                                      LocalDateTime beforePublish, LocalDateTime afterPublish) {
            assertThat(published.createdAt()).isEqualTo(original.createdAt());
            assertThat(published.modifiedAt()).isEqualTo(original.modifiedAt());
            assertThat(published.publishedAt()).isNotNull();
            assertThat(published.publishedAt()).isBetween(beforePublish, afterPublish);
        }
    }

    @Nested
    class 값_객체_특성 {
        @Test
        void 동일한_시간_정보를_가진_메타데이터는_같다() {
            LocalDateTime now = LocalDateTime.now();
            PostMetaData metaData1 = new PostMetaData(now, null, null);
            PostMetaData metaData2 = new PostMetaData(now, null, null);

            assertThat(metaData1).isEqualTo(metaData2);
            assertThat(metaData1.hashCode()).isEqualTo(metaData2.hashCode());
        }

        @Test
        void 다른_시간_정보를_가진_메타데이터는_다르다() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime later = now.plusMinutes(1);

            PostMetaData metaData1 = new PostMetaData(now, null, null);
            PostMetaData metaData2 = new PostMetaData(later, null, null);

            assertThat(metaData1).isNotEqualTo(metaData2);
        }

        @Test
        void 불변_객체로_동작한다() {
            PostMetaData original = PostMetaData.create();

            PostMetaData modified = original.updateModifiedAt();
            PostMetaData published = original.updatePublishedAt();

            assertThat(original).isNotSameAs(modified);
            assertThat(original).isNotSameAs(published);
            assertThat(modified).isNotSameAs(published);
        }
    }

    @Nested
    class 시간_순서_검증 {
        @Test
        void 생성일시는_항상_수정일시보다_이전이거나_같다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData modified = original.updateModifiedAt();

            assertThat(modified.createdAt()).isBeforeOrEqualTo(modified.modifiedAt());
        }

        @Test
        void 생성일시는_항상_발행일시보다_이전이거나_같다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData published = original.updatePublishedAt();

            assertThat(published.createdAt()).isBeforeOrEqualTo(published.publishedAt());
        }

        @Test
        void 수정_후_발행한_경우_시간_순서가_올바르다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData modified = original.updateModifiedAt();
            PostMetaData published = modified.updatePublishedAt();

            assertThatTimeOrderIsCorrect(published);
        }

        @Test
        void 발행_후_수정한_경우에는_수정일시가_가장_최신이다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData published = original.updatePublishedAt();

            // 시간차를 만들기 위한 지연
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PostMetaData modified = published.updateModifiedAt();

            assertThat(modified.modifiedAt()).isAfter(modified.publishedAt());
        }

        private void assertThatTimeOrderIsCorrect(PostMetaData metaData) {
            assertThat(metaData.createdAt()).isBeforeOrEqualTo(metaData.modifiedAt());
            assertThat(metaData.modifiedAt()).isBeforeOrEqualTo(metaData.publishedAt());
        }
    }

    @Nested
    class 상태_확인 {
        @Test
        void 수정되었는지_확인할_수_있다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData modified = original.updateModifiedAt();

            assertThat(isModified(original)).isFalse();
            assertThat(isModified(modified)).isTrue();
        }

        @Test
        void 발행되었는지_확인할_수_있다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData published = original.updatePublishedAt();

            assertThat(isPublished(original)).isFalse();
            assertThat(isPublished(published)).isTrue();
        }

        @Test
        void 초안_상태인지_확인할_수_있다() {
            PostMetaData original = PostMetaData.create();
            PostMetaData modified = original.updateModifiedAt();
            PostMetaData published = original.updatePublishedAt();

            assertThat(isDraft(original)).isTrue();
            assertThat(isDraft(modified)).isTrue();
            assertThat(isDraft(published)).isFalse();
        }

        private boolean isModified(PostMetaData metaData) {
            return metaData.modifiedAt() != null;
        }

        private boolean isPublished(PostMetaData metaData) {
            return metaData.publishedAt() != null;
        }

        private boolean isDraft(PostMetaData metaData) {
            return metaData.publishedAt() == null;
        }
    }
}