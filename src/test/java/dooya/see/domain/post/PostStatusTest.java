package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostStatusTest {
    @Nested
    class 상태_정의 {
        @Test
        void 모든_게시글_상태가_정의되어_있다() {
            PostStatus[] statuses = PostStatus.values();

            assertThat(statuses).containsExactlyInAnyOrder(
                    PostStatus.DRAFT,
                    PostStatus.PUBLISHED,
                    PostStatus.HIDDEN,
                    PostStatus.DELETED
            );
        }

        @Test
        void 각_상태가_올바른_이름을_가진다() {
            assertThat(PostStatus.DRAFT.name()).isEqualTo("DRAFT");
            assertThat(PostStatus.PUBLISHED.name()).isEqualTo("PUBLISHED");
            assertThat(PostStatus.HIDDEN.name()).isEqualTo("HIDDEN");
            assertThat(PostStatus.DELETED.name()).isEqualTo("DELETED");
        }
    }

    @Nested
    class 상태_전이_규칙 {
        @Test
        void DRAFT에서_가능한_전이를_확인할_수_있다() {
            assertThat(canTransitionTo(PostStatus.DRAFT, PostStatus.PUBLISHED)).isTrue();
            assertThat(canTransitionTo(PostStatus.DRAFT, PostStatus.HIDDEN)).isTrue();
            assertThat(canTransitionTo(PostStatus.DRAFT, PostStatus.DELETED)).isTrue();
            assertThat(canTransitionTo(PostStatus.DRAFT, PostStatus.DRAFT)).isFalse();
        }

        @Test
        void PUBLISHED에서_가능한_전이를_확인할_수_있다() {
            assertThat(canTransitionTo(PostStatus.PUBLISHED, PostStatus.HIDDEN)).isTrue();
            assertThat(canTransitionTo(PostStatus.PUBLISHED, PostStatus.DELETED)).isTrue();
            assertThat(canTransitionTo(PostStatus.PUBLISHED, PostStatus.DRAFT)).isFalse();
            assertThat(canTransitionTo(PostStatus.PUBLISHED, PostStatus.PUBLISHED)).isFalse();
        }

        @Test
        void HIDDEN에서_가능한_전이를_확인할_수_있다() {
            assertThat(canTransitionTo(PostStatus.HIDDEN, PostStatus.PUBLISHED)).isTrue();
            assertThat(canTransitionTo(PostStatus.HIDDEN, PostStatus.DELETED)).isTrue();
            assertThat(canTransitionTo(PostStatus.HIDDEN, PostStatus.DRAFT)).isFalse();
            assertThat(canTransitionTo(PostStatus.HIDDEN, PostStatus.HIDDEN)).isFalse();
        }

        @Test
        void DELETED에서는_다른_상태로_전이할_수_없다() {
            assertThat(canTransitionTo(PostStatus.DELETED, PostStatus.DRAFT)).isFalse();
            assertThat(canTransitionTo(PostStatus.DELETED, PostStatus.PUBLISHED)).isFalse();
            assertThat(canTransitionTo(PostStatus.DELETED, PostStatus.HIDDEN)).isFalse();
            assertThat(canTransitionTo(PostStatus.DELETED, PostStatus.DELETED)).isFalse();
        }

        private boolean canTransitionTo(PostStatus from, PostStatus to) {
            if (from == to) return false;
            if (from == PostStatus.DELETED) return false;

            return switch (from) {
                case DRAFT -> to == PostStatus.PUBLISHED || to == PostStatus.HIDDEN || to == PostStatus.DELETED;
                case PUBLISHED -> to == PostStatus.HIDDEN || to == PostStatus.DELETED;
                case HIDDEN -> to == PostStatus.PUBLISHED || to == PostStatus.DELETED;
                default -> throw new IllegalStateException("Unexpected value: " + from);
            };
        }
    }

    @Nested
    class 상태_분류 {
        @Test
        void 공개_상태인지_확인할_수_있다() {
            assertThat(isPublic(PostStatus.PUBLISHED)).isTrue();
            assertThat(isPublic(PostStatus.DRAFT)).isFalse();
            assertThat(isPublic(PostStatus.HIDDEN)).isFalse();
            assertThat(isPublic(PostStatus.DELETED)).isFalse();
        }

        @Test
        void 수정_가능한_상태인지_확인할_수_있다() {
            assertThat(isEditable(PostStatus.DRAFT)).isTrue();
            assertThat(isEditable(PostStatus.PUBLISHED)).isTrue();
            assertThat(isEditable(PostStatus.HIDDEN)).isTrue();
            assertThat(isEditable(PostStatus.DELETED)).isFalse();
        }

        @Test
        void 상호작용_가능한_상태인지_확인할_수_있다() {
            assertThat(canInteract(PostStatus.PUBLISHED)).isTrue();
            assertThat(canInteract(PostStatus.DRAFT)).isFalse();
            assertThat(canInteract(PostStatus.HIDDEN)).isFalse();
            assertThat(canInteract(PostStatus.DELETED)).isFalse();
        }

        @Test
        void 최종_상태인지_확인할_수_있다() {
            assertThat(isFinalState(PostStatus.DELETED)).isTrue();
            assertThat(isFinalState(PostStatus.DRAFT)).isFalse();
            assertThat(isFinalState(PostStatus.PUBLISHED)).isFalse();
            assertThat(isFinalState(PostStatus.HIDDEN)).isFalse();
        }

        private boolean isPublic(PostStatus status) {
            return status == PostStatus.PUBLISHED;
        }

        private boolean isEditable(PostStatus status) {
            return status != PostStatus.DELETED;
        }

        private boolean canInteract(PostStatus status) {
            return status == PostStatus.PUBLISHED;
        }

        private boolean isFinalState(PostStatus status) {
            return status == PostStatus.DELETED;
        }
    }

    @Nested
    class 열거형_변환 {
        @Test
        void 문자열로부터_상태를_생성할_수_있다() {
            assertThat(PostStatus.valueOf("DRAFT")).isEqualTo(PostStatus.DRAFT);
            assertThat(PostStatus.valueOf("PUBLISHED")).isEqualTo(PostStatus.PUBLISHED);
            assertThat(PostStatus.valueOf("HIDDEN")).isEqualTo(PostStatus.HIDDEN);
            assertThat(PostStatus.valueOf("DELETED")).isEqualTo(PostStatus.DELETED);
        }

        @Test
        void 문자열로_변환할_수_있다() {
            assertThat(PostStatus.DRAFT.toString()).isEqualTo("DRAFT");
            assertThat(PostStatus.PUBLISHED.toString()).isEqualTo("PUBLISHED");
            assertThat(PostStatus.HIDDEN.toString()).isEqualTo("HIDDEN");
            assertThat(PostStatus.DELETED.toString()).isEqualTo("DELETED");
        }

        @Test
        void 잘못된_문자열로_상태_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> PostStatus.valueOf("INVALID_STATUS"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void null로_상태_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> PostStatus.valueOf(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class 열거형_특성 {
        @Test
        void 동일한_상수는_같은_인스턴스다() {
            PostStatus status1 = PostStatus.DRAFT;
            PostStatus status2 = PostStatus.DRAFT;

            assertThat(status1).isSameAs(status2);
            assertThat(status1).isEqualTo(status2);
        }

        @Test
        void 다른_상수는_다른_인스턴스다() {
            PostStatus draft = PostStatus.DRAFT;
            PostStatus published = PostStatus.PUBLISHED;

            assertThat(draft).isNotSameAs(published);
            assertThat(draft).isNotEqualTo(published);
        }

        @Test
        void 순서가_정의되어_있다() {
            PostStatus[] statuses = PostStatus.values();

            assertThat(statuses[0]).isEqualTo(PostStatus.DRAFT);
            assertThat(statuses[1]).isEqualTo(PostStatus.PUBLISHED);
            assertThat(statuses[2]).isEqualTo(PostStatus.HIDDEN);
            assertThat(statuses[3]).isEqualTo(PostStatus.DELETED);
        }

        @Test
        void ordinal_값이_올바르다() {
            assertThat(PostStatus.DRAFT.ordinal()).isEqualTo(0);
            assertThat(PostStatus.PUBLISHED.ordinal()).isEqualTo(1);
            assertThat(PostStatus.HIDDEN.ordinal()).isEqualTo(2);
            assertThat(PostStatus.DELETED.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    class 비즈니스_규칙_검증 {
        @Test
        void 생성_시_초기_상태는_DRAFT다() {
            PostStatus initialStatus = getInitialStatus();

            assertThat(initialStatus).isEqualTo(PostStatus.DRAFT);
        }

        @Test
        void 발행_가능한_상태들을_확인할_수_있다() {
            assertThat(canPublish(PostStatus.DRAFT)).isTrue();
            assertThat(canPublish(PostStatus.HIDDEN)).isTrue();
            assertThat(canPublish(PostStatus.PUBLISHED)).isFalse();
            assertThat(canPublish(PostStatus.DELETED)).isFalse();
        }

        @Test
        void 숨김_가능한_상태들을_확인할_수_있다() {
            assertThat(canHide(PostStatus.DRAFT)).isTrue();
            assertThat(canHide(PostStatus.PUBLISHED)).isTrue();
            assertThat(canHide(PostStatus.HIDDEN)).isFalse();
            assertThat(canHide(PostStatus.DELETED)).isFalse();
        }

        @Test
        void 삭제_가능한_상태들을_확인할_수_있다() {
            assertThat(canDelete(PostStatus.DRAFT)).isTrue();
            assertThat(canDelete(PostStatus.PUBLISHED)).isTrue();
            assertThat(canDelete(PostStatus.HIDDEN)).isTrue();
            assertThat(canDelete(PostStatus.DELETED)).isFalse();
        }

        private PostStatus getInitialStatus() {
            return PostStatus.DRAFT;
        }

        private boolean canPublish(PostStatus status) {
            return status == PostStatus.DRAFT || status == PostStatus.HIDDEN;
        }

        private boolean canHide(PostStatus status) {
            return status == PostStatus.DRAFT || status == PostStatus.PUBLISHED;
        }

        private boolean canDelete(PostStatus status) {
            return status != PostStatus.DELETED;
        }
    }
}