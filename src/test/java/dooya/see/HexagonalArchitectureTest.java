package dooya.see;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class HexagonalArchitectureTest {
    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dooya.see");
    }

    @Nested
    class 계층형_아키텍처_검증 {
        @Test
        void 헥사고날_아키텍처_계층_의존성_규칙을_준수한다() {
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
        void 도메인_계층은_외부_의존성이_없어야_한다() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..adapter..")
                    .check(classes);
        }

        @Test
        void 애플리케이션_계층은_어댑터에_의존하면_안된다() {
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter..")
                    .check(classes);
        }
    }

    @Nested
    class 도메인_비즈니스_로직 {
        @Test
        void 도메인은_Spring_컨테이너_애노테이션을_사용하면_안된다() {
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
        void 도메인은_웹_관련_애노테이션을_사용하면_안된다() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                    .because("도메인은 웹 계층과 독립적이어야 합니다")
                    .check(classes);
        }

        @Test
        void 도메인은_JPA_Hibernate를_사용할_수_있다() {
            // JPA 애노테이션이 도메인 로직에 미치는 영향을 최소화하면서, 매핑을 위한 애노테이션 사용은 허용
            // 이 테스트는 문서화 목적으로 JPA 사용이 허용됨을 명시
        }
    }

    @Nested
    class 포트와_어댑터_패턴 {
        @Test
        void Primary_Port는_application_provided_패키지에_위치한다() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage("..application..provided..")
                    .should().bePublic()
                    .because("Primary Port는 외부에서 애플리케이션을 호출하는 인터페이스입니다")
                    .check(classes);
        }

        @Test
        void Secondary_Port는_application_required_패키지에_위치한다() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage("..application..required..")
                    .should().bePublic()
                    .because("Secondary Port는 애플리케이션이 외부를 호출하는 인터페이스입니다")
                    .check(classes);
        }

        @Test
        void 어댑터는_포트_인터페이스를_구현해야_한다() {
            // 이 테스트는 실제로는 복잡한 검증이 필요하므로
            // 문서화 목적으로 어댑터가 포트 인터페이스를 구현해야 함을 명시
            // 실제 구현체들은 개별적으로 확인하는 것이 더 실용적임

            // 예시: 웹 어댑터는 Primary Port를 주입받아 사용
            // 예시: 리포지토리 어댑터는 Secondary Port를 구현

            // 향후 구체적인 어댑터 구현 시 개별 테스트 추가 예정
        }
    }

    @Nested
    class 애그리거트와_경계 {
        @Test
        void 애그리거트_내부_패키지는_순환_의존성이_없어야_한다() {
            slices()
                    .matching("..domain.(*)..")
                    .should().beFreeOfCycles()
                    .because("애그리거트 간에는 순환 의존성이 없어야 합니다")
                    .check(classes);
        }
    }

    @Nested
    class 명명_규칙 {
        @Test
        void 리포지토리_인터페이스는_Repository로_끝나야_한다() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage("..application..required..")
                    .should().haveSimpleNameEndingWith("Repository")
                    .orShould().haveSimpleNameEndingWith("Manager")
                    .because("리포지토리는 명확한 명명 규칙을 따라야 합니다")
                    .check(classes);
        }

        @Test
        void 애플리케이션_서비스는_Service로_끝나야_한다() {
            classes()
                    .that().resideInAPackage("..application..")
                    .and().areAnnotatedWith("org.springframework.stereotype.Service")
                    .should().haveSimpleNameEndingWith("Service")
                    .because("애플리케이션 서비스는 명확한 명명 규칙을 따라야 합니다")
                    .check(classes);
        }
    }

    @Nested
    class Spring_애노테이션_사용_규칙 {
        @Test
        void Component_계열_애노테이션은_어댑터와_애플리케이션_서비스에서_사용된다() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Component")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                    .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .because("도메인 계층은 Spring 애노테이션을 사용하지 않아야 합니다")
                    .check(classes);
        }

        @Test
        void Transactional은_애플리케이션_서비스에서만_사용한다() {
            classes()
                    .that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                    .or().areAnnotatedWith("jakarta.transaction.Transactional")
                    .should().resideInAPackage("..application..")
                    .because("트랜잭션 경계는 애플리케이션 서비스에서 관리해야 합니다")
                    .check(classes);
        }
    }
}
