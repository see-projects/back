package dooya.see.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractEntityTest {
    @DisplayName("같은 객체 참조인 경우 equals는 true를 반환한다")
    @Test
    void sameReference() {
        TestEntity entity = new TestEntity();

        assertThat(entity.equals(entity)).isTrue();
    }

    @DisplayName("null과 비교하면 equals는 false를 반환한다")
    @Test
    void equalsNull() {
        TestEntity entity = new TestEntity();

        assertThat(entity.equals(null)).isFalse();
    }

    @DisplayName("다른 클래스 타입과 비교하면 equals는 false를 반환한다")
    @Test
    void differentClass() {
        TestEntity entity = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();

        assertThat(entity.equals(anotherEntity)).isFalse();
    }

    @DisplayName("ID가 null인 엔티티들은 서로 다르다")
    @Test
    void bothNullId() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isNull();
        assertThat(entity2.getId()).isNull();
    }

    @DisplayName("한쪽만 ID가 null인 경우 서로 다르다")
    @Test
    void oneNullId() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity2, 1L);

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isNull();
        assertThat(entity2.getId()).isEqualTo(1L);
    }

    @DisplayName("같은 ID를 가진 엔티티들은 같다")
    @Test
    void sameId() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity1, 1L);
        setId(entity2, 1L);

        assertThat(entity1.equals(entity2)).isTrue();
        assertThat(entity1.getId()).isEqualTo(1L);
        assertThat(entity2.getId()).isEqualTo(1L);
    }

    @DisplayName("다른 ID를 가진 엔티티들은 다르다")
    @Test
    void differentId() throws Exception {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        setId(entity1, 1L);
        setId(entity2, 2L);

        assertThat(entity1.equals(entity2)).isFalse();
        assertThat(entity1.getId()).isEqualTo(1L);
        assertThat(entity2.getId()).isEqualTo(2L);
    }

    @DisplayName("같은 클래스의 다른 인스턴스들은 구조적으로 동등하다")
    @Test
    void structuralEquality() throws Exception {
        ProxySimulationEntity realEntity = new ProxySimulationEntity();
        ProxySimulationEntity proxyEntity = new ProxySimulationEntity();
        setId(realEntity, 1L);
        setId(proxyEntity, 1L);

        assertThat(realEntity.equals(proxyEntity)).isTrue();
        assertThat(proxyEntity.equals(realEntity)).isTrue();
    }

    @DisplayName("서로 다른 클래스 타입은 같은 ID라도 다르다")
    @Test
    void differentClassSameId() throws Exception {
        TestEntity testEntity = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();
        setId(testEntity, 1L);
        setId(anotherEntity, 1L);

        assertThat(testEntity.equals(anotherEntity)).isFalse();
    }

    @DisplayName("hashCode는 클래스 기반으로 일관성을 유지한다")
    @Test
    void hashCodeConsistency() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        AnotherTestEntity anotherEntity = new AnotherTestEntity();

        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1.hashCode()).isEqualTo(TestEntity.class.hashCode());

        assertThat(entity1.hashCode()).isNotEqualTo(anotherEntity.hashCode());
    }

    @DisplayName("toString 메서드가 정상 동작한다")
    @Test
    void toStringWorksCorrectly() throws Exception {
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
