package dooya.see.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractEntityTest {
    @Nested
    class equals_동등성_검증 {
        @Test
        void 같은_객체_참조인_경우_true를_반환한다() {
            TestEntity entity = new TestEntity();

            assertThat(entity.equals(entity)).isTrue();
        }

        @Test
        void null과_비교하면_false를_반환한다() {
            TestEntity entity = new TestEntity();

            assertThat(entity.equals(null)).isFalse();
        }

        @Test
        void 다른_클래스_타입과_비교하면_false를_반환한다() {
            TestEntity entity = new TestEntity();
            AnotherTestEntity anotherEntity = new AnotherTestEntity();

            assertThat(entity.equals(anotherEntity)).isFalse();
        }
    }

    @Nested
    class ID_기반_동등성 {
        @Test
        void ID가_null인_엔티티들은_서로_다르다() {
            TestEntity entity1 = new TestEntity();
            TestEntity entity2 = new TestEntity();

            assertThat(entity1.equals(entity2)).isFalse();
            assertThatBothEntitiesHaveNullId(entity1, entity2);
        }

        @Test
        void 한쪽만_ID가_null인_경우_서로_다르다() {
            TestEntity entity1 = new TestEntity();
            TestEntity entity2 = createEntityWithId(1L);

            assertThat(entity1.equals(entity2)).isFalse();
            assertThat(entity1.getId()).isNull();
            assertThat(entity2.getId()).isEqualTo(1L);
        }

        @Test
        void 같은_ID를_가진_엔티티들은_같다() {
            TestEntity entity1 = createEntityWithId(1L);
            TestEntity entity2 = createEntityWithId(1L);

            assertThat(entity1.equals(entity2)).isTrue();
            assertThatBothEntitiesHaveSameId(entity1, entity2, 1L);
        }

        @Test
        void 다른_ID를_가진_엔티티들은_다르다() {
            TestEntity entity1 = createEntityWithId(1L);
            TestEntity entity2 = createEntityWithId(2L);

            assertThat(entity1.equals(entity2)).isFalse();
            assertThat(entity1.getId()).isEqualTo(1L);
            assertThat(entity2.getId()).isEqualTo(2L);
        }

        @Test
        void 같은_클래스의_다른_인스턴스들은_ID가_같으면_동등하다() {
            ProxySimulationEntity entity1 = createProxyEntityWithId(1L);
            ProxySimulationEntity entity2 = createProxyEntityWithId(1L);

            assertThatEntitiesAreSymmetricallyEqual(entity1, entity2);
        }

        @Test
        void 서로_다른_클래스는_같은_ID라도_다르다() {
            TestEntity testEntity = createEntityWithId(1L);
            AnotherTestEntity anotherEntity = createAnotherEntityWithId(1L);

            assertThat(testEntity.equals(anotherEntity)).isFalse();
        }

        private void assertThatBothEntitiesHaveNullId(TestEntity entity1, TestEntity entity2) {
            assertThat(entity1.getId()).isNull();
            assertThat(entity2.getId()).isNull();
        }

        private void assertThatBothEntitiesHaveSameId(TestEntity entity1, TestEntity entity2, Long expectedId) {
            assertThat(entity1.getId()).isEqualTo(expectedId);
            assertThat(entity2.getId()).isEqualTo(expectedId);
        }

        private void assertThatEntitiesAreSymmetricallyEqual(AbstractEntity entity1, AbstractEntity entity2) {
            assertThat(entity1.equals(entity2)).isTrue();
            assertThat(entity2.equals(entity1)).isTrue();
        }
    }

    @Nested
    class hashCode_일관성 {
        @Test
        void hashCode는_클래스_기반으로_일관성을_유지한다() {
            TestEntity entity1 = new TestEntity();
            TestEntity entity2 = new TestEntity();
            AnotherTestEntity anotherEntity = new AnotherTestEntity();

            assertThatSameClassHashCodesAreEqual(entity1, entity2);
            assertThatDifferentClassHashCodesAreDifferent(entity1, anotherEntity);
        }

        @Test
        void 같은_클래스는_항상_같은_hashCode를_가진다() {
            TestEntity entity = new TestEntity();

            assertThat(entity.hashCode()).isEqualTo(TestEntity.class.hashCode());
        }

        private void assertThatSameClassHashCodesAreEqual(TestEntity entity1, TestEntity entity2) {
            assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
            assertThat(entity1.hashCode()).isEqualTo(TestEntity.class.hashCode());
        }

        private void assertThatDifferentClassHashCodesAreDifferent(TestEntity entity1, AnotherTestEntity anotherEntity) {
            assertThat(entity1.hashCode()).isNotEqualTo(anotherEntity.hashCode());
        }
    }

    @Nested
    class toString_구현 {
        @Test
        void toString_메서드가_올바른_정보를_포함한다() {
            TestEntity entity = createEntityWithId(42L);

            String result = entity.toString();

            assertThatToStringContainsExpectedInfo(result);
        }

        @Test
        void ID가_null인_경우_toString이_정상_동작한다() {
            TestEntity entity = new TestEntity();

            String result = entity.toString();

            assertThat(result).contains("AbstractEntity");
            assertThat(result).contains("id=null");
        }

        private void assertThatToStringContainsExpectedInfo(String result) {
            assertThat(result).contains("AbstractEntity");
            assertThat(result).contains("id=42");
        }
    }

    @Nested
    class 엔티티_동등성_계약 {
        @Test
        void equals_계약을_준수한다() {
            TestEntity entity1 = createEntityWithId(1L);
            TestEntity entity2 = createEntityWithId(1L);
            TestEntity entity3 = createEntityWithId(1L);

            // 반사성(Reflexive): x.equals(x) == true
            assertThat(entity1.equals(entity1)).isTrue();

            // 대칭성(Symmetric): x.equals(y) == y.equals(x)
            assertThat(entity1.equals(entity2)).isEqualTo(entity2.equals(entity1));

            // 이행성(Transitive): x.equals(y) && y.equals(z) => x.equals(z)
            assertThat(entity1.equals(entity2)).isTrue();
            assertThat(entity2.equals(entity3)).isTrue();
            assertThat(entity1.equals(entity3)).isTrue();

            // 일관성(Consistent): 여러 번 호출해도 동일한 결과
            assertThat(entity1.equals(entity2)).isEqualTo(entity1.equals(entity2));

            // null에 대한 처리
            assertThat(entity1.equals(null)).isFalse();
        }

        @Test
        void hashCode_계약을_준수한다() {
            TestEntity entity1 = createEntityWithId(1L);
            TestEntity entity2 = createEntityWithId(1L);

            // equals가 true이면 hashCode도 같아야 함
            if (entity1.equals(entity2)) {
                assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
            }

            // 일관성: 여러 번 호출해도 동일한 결과
            int firstCall = entity1.hashCode();
            int secondCall = entity1.hashCode();
            assertThat(firstCall).isEqualTo(secondCall);
        }
    }

    // 헬퍼 메서드들
    private TestEntity createEntityWithId(Long id) {
        TestEntity entity = new TestEntity();
        setEntityId(entity, id);
        return entity;
    }

    private AnotherTestEntity createAnotherEntityWithId(Long id) {
        AnotherTestEntity entity = new AnotherTestEntity();
        setEntityId(entity, id);
        return entity;
    }

    private ProxySimulationEntity createProxyEntityWithId(Long id) {
        ProxySimulationEntity entity = new ProxySimulationEntity();
        setEntityId(entity, id);
        return entity;
    }

    private void setEntityId(AbstractEntity entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    // 테스트용 클래스들
    static class TestEntity extends AbstractEntity {
    }

    static class AnotherTestEntity extends AbstractEntity {
    }

    static class ProxySimulationEntity extends AbstractEntity {
    }
}