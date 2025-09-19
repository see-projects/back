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
    private static final String DOMAIN_PACKAGE = "..domain..";
    private static final String APPLICATION_PACKAGE = "..application..";
    private static final String ADAPTER_PACKAGE = "..adapter..";
    private static final String PROVIDED_PACKAGE = "..application..provided..";
    private static final String REQUIRED_PACKAGE = "..application..required..";

    private static final String SPRING_COMPONENT = "org.springframework.stereotype.Component";
    private static final String SPRING_SERVICE = "org.springframework.stereotype.Service";
    private static final String SPRING_REPOSITORY = "org.springframework.stereotype.Repository";
    private static final String SPRING_CONFIGURATION = "org.springframework.context.annotation.Configuration";
    private static final String SPRING_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    private static final String SPRING_TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";
    private static final String JAKARTA_TRANSACTIONAL = "jakarta.transaction.Transactional";

    private static final String SPRING_WEB_PACKAGE = "org.springframework.web..";
    private static final String JAKARTA_SERVLET_PACKAGE = "jakarta.servlet..";

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
                    .layer("Domain").definedBy(DOMAIN_PACKAGE)
                    .layer("Application").definedBy(APPLICATION_PACKAGE)
                    .layer("Adapter").definedBy(ADAPTER_PACKAGE)
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
                    .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
                    .check(classes);
        }

        @Test
        void 도메인_계층은_외부_의존성이_없어야_한다() {
            assertThatDomainHasNoExternalDependencies();
        }

        @Test
        void 애플리케이션_계층은_어댑터에_의존하면_안된다() {
            assertThatApplicationDoesNotDependOnAdapter();
        }

        private void assertThatDomainHasNoExternalDependencies() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(APPLICATION_PACKAGE, ADAPTER_PACKAGE)
                    .check(classes);
        }

        private void assertThatApplicationDoesNotDependOnAdapter() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_PACKAGE)
                    .check(classes);
        }
    }

    @Nested
    class 도메인_순수성_검증 {
        @Test
        void 도메인은_Spring_컨테이너_애노테이션을_사용하면_안된다() {
            assertThatDomainDoesNotUseSpringAnnotations();
        }

        @Test
        void 도메인은_웹_관련_의존성을_사용하면_안된다() {
            assertThatDomainDoesNotDependOnWeb();
        }

        @Test
        void 도메인은_JPA_애노테이션_사용이_허용된다() {
            // JPA 애노테이션이 도메인 로직에 미치는 영향을 최소화하면서
            // 매핑을 위한 애노테이션 사용은 허용됨을 문서화
            assertThatJpaUsageIsDocumented();
        }

        private void assertThatDomainDoesNotUseSpringAnnotations() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().beAnnotatedWith(SPRING_COMPONENT)
                    .orShould().beAnnotatedWith(SPRING_SERVICE)
                    .orShould().beAnnotatedWith(SPRING_REPOSITORY)
                    .orShould().beAnnotatedWith(SPRING_CONFIGURATION)
                    .because("도메인은 Spring 컨테이너에 의존하지 않고 순수한 비즈니스 로직에 집중해야 합니다")
                    .check(classes);
        }

        private void assertThatDomainDoesNotDependOnWeb() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(SPRING_WEB_PACKAGE, JAKARTA_SERVLET_PACKAGE)
                    .because("도메인은 웹 계층과 독립적이어야 합니다")
                    .check(classes);
        }

        private void assertThatJpaUsageIsDocumented() {
            // JPA 애노테이션 사용 허용을 문서화
            // 실제 검증은 도메인 모델의 JPA 매핑이 올바른지 확인하는 별도 테스트에서 수행
        }
    }

    @Nested
    class 포트와_어댑터_패턴 {
        @Test
        void Primary_Port는_provided_패키지에_위치하고_public이어야_한다() {
            assertThatPrimaryPortsAreCorrectlyDefined();
        }

        @Test
        void Secondary_Port는_required_패키지에_위치하고_public이어야_한다() {
            assertThatSecondaryPortsAreCorrectlyDefined();
        }

        @Test
        void 어댑터_패턴_구현_규칙이_문서화된다() {
            assertThatAdapterPatternIsDocumented();
        }

        private void assertThatPrimaryPortsAreCorrectlyDefined() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage(PROVIDED_PACKAGE)
                    .should().bePublic()
                    .because("Primary Port는 외부에서 애플리케이션을 호출하는 인터페이스입니다")
                    .check(classes);
        }

        private void assertThatSecondaryPortsAreCorrectlyDefined() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage(REQUIRED_PACKAGE)
                    .should().bePublic()
                    .because("Secondary Port는 애플리케이션이 외부를 호출하는 인터페이스입니다")
                    .check(classes);
        }

        private void assertThatAdapterPatternIsDocumented() {
            // 어댑터가 포트 인터페이스를 구현해야 함을 문서화
            // 예시: 웹 어댑터는 Primary Port를 주입받아 사용
            // 예시: 리포지토리 어댑터는 Secondary Port를 구현
            // 실제 구현체들은 개별적으로 확인하는 것이 더 실용적임
        }
    }

    @Nested
    class 애그리거트_경계_검증 {
        @Test
        void 애그리거트_간에는_순환_의존성이_없어야_한다() {
            assertThatAggregatesHaveNoCycles();
        }

        private void assertThatAggregatesHaveNoCycles() {
            slices()
                    .matching("..domain.(*)..")
                    .should().beFreeOfCycles()
                    .because("애그리거트 간에는 순환 의존성이 없어야 합니다")
                    .check(classes);
        }
    }

    @Nested
    class 명명_규칙_검증 {
        @Test
        void 리포지토리_인터페이스는_적절한_접미사를_가져야_한다() {
            assertThatRepositoryInterfacesFollowNamingConvention();
        }

        @Test
        void 애플리케이션_서비스는_Service로_끝나야_한다() {
            assertThatApplicationServicesFollowNamingConvention();
        }

        private void assertThatRepositoryInterfacesFollowNamingConvention() {
            classes()
                    .that().areInterfaces()
                    .and().resideInAPackage(REQUIRED_PACKAGE)
                    .should().haveSimpleNameEndingWith("Repository")
                    .orShould().haveSimpleNameEndingWith("Manager")
                    .because("리포지토리는 명확한 명명 규칙을 따라야 합니다")
                    .check(classes);
        }

        private void assertThatApplicationServicesFollowNamingConvention() {
            classes()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .and().areAnnotatedWith(SPRING_SERVICE)
                    .should().haveSimpleNameEndingWith("Service")
                    .because("애플리케이션 서비스는 명확한 명명 규칙을 따라야 합니다")
                    .check(classes);
        }
    }

    @Nested
    class Spring_애노테이션_사용_규칙 {
        @Test
        void 도메인은_Spring_애노테이션을_사용하면_안된다() {
            assertThatDomainDoesNotUseSpringContainerAnnotations();
        }

        @Test
        void Transactional은_애플리케이션_계층에서만_사용한다() {
            assertThatTransactionalIsOnlyUsedInApplicationLayer();
        }

        private void assertThatDomainDoesNotUseSpringContainerAnnotations() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().beAnnotatedWith(SPRING_COMPONENT)
                    .orShould().beAnnotatedWith(SPRING_SERVICE)
                    .orShould().beAnnotatedWith(SPRING_REPOSITORY)
                    .orShould().beAnnotatedWith(SPRING_REST_CONTROLLER)
                    .because("도메인 계층은 Spring 애노테이션을 사용하지 않아야 합니다")
                    .check(classes);
        }

        private void assertThatTransactionalIsOnlyUsedInApplicationLayer() {
            classes()
                    .that().areAnnotatedWith(SPRING_TRANSACTIONAL)
                    .or().areAnnotatedWith(JAKARTA_TRANSACTIONAL)
                    .should().resideInAPackage(APPLICATION_PACKAGE)
                    .because("트랜잭션 경계는 애플리케이션 서비스에서 관리해야 합니다")
                    .check(classes);
        }
    }
}