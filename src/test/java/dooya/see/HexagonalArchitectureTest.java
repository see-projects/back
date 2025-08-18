package dooya.see;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        @DisplayName("헥사고날 아키텍처 계층 의존성 규칙 검증")
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
    }
}
