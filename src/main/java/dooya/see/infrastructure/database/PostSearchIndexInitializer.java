package dooya.see.infrastructure.database;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Ensures that the MySQL table backing {@code Post} has the indexes required for fast
 * full-text lookups combined with the auxiliary filters used in {@code searchWithFullTextIndex}.
 * <p>
 * The initializer is idempotent: it checks the current schema before attempting to create
 * each index so that it can safely run on every application start.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostSearchIndexInitializer {

    private static final String TABLE_NAME = "post";
    private static final Locale LOCALE = Locale.US;

    private final JdbcTemplate jdbcTemplate;
    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    void ensureIndexes() {
        if (!isMySqlDialect()) {
            log.debug("[search-index] skipping index initialization (dialect is not MySQL)");
            return;
        }

        // Auxiliary indexes for equality filters and ordering.
        createIndexIfMissing("idx_post_status", "CREATE INDEX idx_post_status ON post (status)");
        createIndexIfMissing("idx_post_category", "CREATE INDEX idx_post_category ON post (category)");
        createIndexIfMissing("idx_post_member_id", "CREATE INDEX idx_post_member_id ON post (member_id)");
        createIndexIfMissing("idx_post_created_at", "CREATE INDEX idx_post_created_at ON post (created_at)");

        // Composite index that covers the most common filter combination used together with pagination.
        createIndexIfMissing(
                "idx_post_status_created_at",
                "CREATE INDEX idx_post_status_created_at ON post (status, created_at DESC)"
        );

        // Full-text index for title/body searches.
        createIndexIfMissing(
                "idx_post_fulltext_title_body",
                "CREATE FULLTEXT INDEX idx_post_fulltext_title_body ON post(title, body)"
        );
    }

    private boolean isMySqlDialect() {
        return detectDialectName()
                .map(name -> name.toLowerCase(LOCALE).contains("mysql"))
                .orElse(false);
    }

    private Optional<String> detectDialectName() {
        Map<String, Object> properties = entityManagerFactory.getProperties();
        Object configuredDialect = properties.get("hibernate.dialect");
        if (configuredDialect != null) {
            return Optional.of(configuredDialect.toString());
        }
        try {
            SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
            return Optional.ofNullable(sessionFactory.getJdbcServices().getDialect().getClass().getName());
        } catch (RuntimeException ex) {
            log.debug("[search-index] failed to unwrap SessionFactoryImplementor for dialect detection", ex);
            return Optional.empty();
        }
    }

    private void createIndexIfMissing(String indexName, String ddl) {
        if (indexExists(indexName)) {
            log.trace("[search-index] index '{}' already present", indexName);
            return;
        }
        try {
            jdbcTemplate.execute(ddl);
            log.info("[search-index] created index '{}'", indexName);
        } catch (DataAccessException ex) {
            log.warn("[search-index] failed to create index '{}' using statement: {}", indexName, ddl, ex);
        }
    }

    private boolean indexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.statistics
                        WHERE table_schema = DATABASE()
                          AND table_name = ?
                          AND index_name = ?
                        """,
                Integer.class,
                TABLE_NAME,
                indexName
        );
        return count != null && count > 0;
    }
}
