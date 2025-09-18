package dooya.see.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractEntityTest {
    @Test
    void 같은_객체_참조인_경우_equals는_true를_반환한다() {
        TestEntity entity = new TestEntity();

        assertThat(entity.equals(entity)).isTrue();
    }

    @Test
    void null과_비교하면_equals는_false를_반환한다() {
        TestEntity entity = new TestEntity();

        assertThat(entity.equals(null)).isFalse();
    }

    @Test
    void 다른_클래스_타입과_비교하면_equals는_false를_반환한다() {
        TestEntity entity = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();

        assertThat(entity.equals(anotherEntity)).isFalse();
    }

    @Test
    void ID가_null인_엔티티들은_서로_다르다() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isNull();
        assertThat(entity2.getId()).isNull();
    }

    @Test
    void 한쪽만_ID가_null인_경우_서로_다르다() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity2, 1L);

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isNull();
        assertThat(entity2.getId()).isEqualTo(1L);
    }

    @Test
    void 같은_ID를_가진_엔티티들은_같다() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity1, 1L);
        setId(entity2, 1L);

        assertThat(entity1.equals(entity2)).isTrue();
        assertThat(entity1.getId()).isEqualTo(1L);
        assertThat(entity2.getId()).isEqualTo(1L);
    }

    @Test
    void 다른_ID를_가진_엔티티들은_다르다() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity1, 1L);
        setId(entity2, 2L);

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isEqualTo(1L);
        assertThat(entity2.getId()).isEqualTo(2L);
    }

    @Test
    void 같은_클래스의_다른_인스턴스들은_구조적으로_동등하다() throws Exception {
        ProxySimulationEntity realEntity = new ProxySimulationEntity();
        ProxySimulationEntity proxyEntity = new ProxySimulationEntity();
        setId(realEntity, 1L);
        setId(proxyEntity, 1L);

        assertThat(realEntity.equals(proxyEntity)).isTrue();
        assertThat(proxyEntity.equals(realEntity)).isTrue();
    }

    @Test
    void 서로_다른_클래스_타입은_같은_ID라도_다르다() throws Exception {
        TestEntity testEntity = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();
        setId(testEntity, 1L);
        setId(anotherEntity, 1L);

        assertThat(testEntity.equals(anotherEntity)).isFalse();
    }

    @Test
    void hashCode는_클래스_기반으로_일관성을_유지한다() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();

        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1.hashCode()).isEqualTo(TestEntity.class.hashCode());

        assertThat(entity1.hashCode()).isNotEqualTo(anotherEntity.hashCode());
    }

    @Test
    void toString_메서드가_정상_동작한다() throws Exception {
        TestEntity entity = new TestEntity();
        setId(entity, 42L);

        String result = entity.toString();

        assertThat(result).contains("AbstractEntity");
        assertThat(result).contains("id=42");
    }

    private void setId(AbstractEntity entity, Long id) throws Exception {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    static class TestEntity extends AbstractEntity {
    }

    static class AnotherTestEntity extends AbstractEntity {
    }

    static class ProxySimulationEntity extends AbstractEntity {
    }
}
