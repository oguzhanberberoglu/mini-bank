package com.main.mini_bank.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// Repairs legacy schemas where account identifiers were stored as bytea.
@Component
public class DatabaseSchemaFixer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaFixer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgres()) {
            return;
        }

        logConnectionInfo();

        if (!tableExistsInAnySchema("accounts")) {
            return;
        }

        fixColumnInAllSchemasIfBytea("accounts", "number", "varchar(50)");
        fixColumnInAllSchemasIfBytea("accounts", "name", "varchar(100)");
    }

    private boolean isPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            return productName != null
                && productName.toLowerCase(Locale.ROOT).contains("postgres");
        } catch (SQLException ex) {
            logger.warn("Failed to detect database product name", ex);
            return false;
        }
    }

    private void logConnectionInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("Database URL: {}", metaData.getURL());
            logger.info("Database user: {}", metaData.getUserName());
        } catch (SQLException ex) {
            logger.warn("Failed to read database metadata", ex);
        }

        try {
            String database = jdbcTemplate.queryForObject("select current_database()", String.class);
            String schema = jdbcTemplate.queryForObject("select current_schema()", String.class);
            logger.info("Current database: {}", database);
            logger.info("Current schema: {}", schema);
        } catch (DataAccessException ex) {
            logger.warn("Failed to query database/schema info", ex);
        }
    }

    private boolean tableExistsInAnySchema(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            """
                select count(*)
                from information_schema.tables
                where table_name = ?
                """,
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }

    private void fixColumnInAllSchemasIfBytea(String tableName, String columnName, String targetType) {
        var schemas = jdbcTemplate.queryForList(
            """
                select distinct table_schema
                from information_schema.columns
                where table_name = ?
                  and column_name = ?
                  and data_type = 'bytea'
                """,
            String.class,
            tableName,
            columnName
        );

        if (schemas.isEmpty()) {
            logger.info("No bytea columns found for {}.{}", tableName, columnName);
            return;
        }

        for (String schema : schemas) {
            String sql = String.format(
                "alter table %s.%s alter column %s type %s using convert_from(%s, 'UTF8')",
                quoteIdentifier(schema),
                quoteIdentifier(tableName),
                quoteIdentifier(columnName),
                targetType,
                quoteIdentifier(columnName)
            );

            try {
                jdbcTemplate.execute(sql);
                logger.info("Updated {}.{} in schema {} to {}", tableName, columnName, schema, targetType);
            } catch (DataAccessException ex) {
                logger.warn("Failed to update {}.{} in schema {} to {}", tableName, columnName, schema, targetType, ex);
            }
        }
    }

    private String quoteIdentifier(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
