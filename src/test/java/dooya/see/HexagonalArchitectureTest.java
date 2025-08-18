package dooya.see;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalArchitectureTest {
    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dooya.see");
    }

    @Nested
    @DisplayName("계층형 아키텍처 검증")
    class LayeredArchitectureTest {
        @Test
        @DisplayName("헥사고날 아키텍처 계층 의존성 규칙을 준수한다")
        void a() {
            Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Adapter").definedBy("..adapter..")

                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
                    .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()

                    .check(classes);
        }

        @Test
        @DisplayName("도메인 계층은 외부 의존성이 없어야 한다")
        void b() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..adapter..")
                    .check(classes);
        }
        
        @Test
        @DisplayName("애플리케이션 계층은 어댑터에 의존하면 안된다")
        void c() {
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("도메인 비즈니스 로직")
    class DomainBusinessLogicTest {
        @Test
        @DisplayName("도메인은 Spring 컨테이너 애노테이션을 사용하면 안된다")
        void d() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Component")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                    .orShould().beAnnotatedWith("org.springframework.context.annotation.Configuration")
                    .because("도메인은 Spring 컨테이너에 의존하지 않고 순수한 비즈니스 로직에 집중해야 합니다")
                    .check(classes);
        }

        @Test
        @DisplayName("도메인은 웹 관련 애노테이션을 사용하면 안된다")
        void e() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                    .because("도메인은 웹 계층과 독립적이어야 합니다")
                    .check(classes);
        }

        @Test
        @DisplayName("도메인은 JPA/Hibernate를 사용할 수 있다")
        void f() {
            // JPA 애노테이션이 도메인 로직에 미치는 영향을 최소화하면서, 매핑을 위한 애노테이션 사용은 허용
            // 이 테스트는 문서화 목적으로 JPA 사용이 허용됨을 명시
        }
    }

    @Nested
    @DisplayName("포트와 어댑터 패턴")
    class PortAndAdapterTest {
        @Test
        @DisplayName("Primary Port는 application.provided 패키지에 위치한다")
        void a() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage("..application..provided..")
                    .should().bePublic()
                    .because("Primary Port는 외부에서 애플리케이션을 호출하는 인터페이스입니다")
                    .check(classes);
        }

        @Test
        @DisplayName("Secondary Post는 application.required 패키지에 위치한다")
        void b() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAnyPackage("..application..required..")
                    .should().bePublic()
                    .because("Secondary Port는 애플리케이션이 외부를 호출하는 인터페이스입니다")
                    .check(classes);
        }
    }
}
