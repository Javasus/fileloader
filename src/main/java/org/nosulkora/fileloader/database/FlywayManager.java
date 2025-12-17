package org.nosulkora.fileloader.database;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlywayManager {

    private static final Logger logger = LoggerFactory.getLogger(FlywayManager.class);
    private static final Flyway flyway;

    static {
        flyway = Flyway.configure()
                .dataSource(
                        "jdbc:mysql://localhost:3307/fileloader",
                        "fileloader_user",
                        "fileloader_password")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .cleanDisabled(false)
                .load();
    }

    public static void runMigration() {
        logger.info("Старт миграций...");
        MigrateResult result = flyway.migrate();
        logger.info("Миграции выполнены.");
        logAppliedMigrations();
    }

    public static void validate() {
        logger.info("Валидация миграций...");
        flyway.validate();
        logger.info("Валидация миграций прошла успешно.");
    }

    public static void clean() {
        logger.info("Очистка БД...");
        flyway.clean();
        logger.info("БД очищенна.");
    }

    private static void logAppliedMigrations() {
        MigrationInfo[] applied = flyway.info().applied();
        if (applied.length > 0) {
            logger.info("Применены миграции: ");
            for (MigrationInfo info : applied) {
                logger.info(" {} - {}", info.getVersion(), info.getDescription());
            }
        }
    }
}
