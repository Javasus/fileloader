package org.nosulkora.fileloader.database;

public class DatabaseSetupTest {
    public static void main(String[] args) {
        System.out.println("=== Database Setup Test ===\n");

        try {
            // 1. Очистка (если была неудачная миграция)
            System.out.println("1. Cleaning previous failed migration...");
            FlywayManager.clean();

            // 2. Запуск миграций
            System.out.println("1. Running Flyway migrations...");
            FlywayManager.runMigration();

            // 3. Проверка Hibernate
            System.out.println("2. Testing Hibernate connection...");
            boolean connected = DatabaseManager.isDatabaseAvailable();

            if (connected) {
                System.out.println("✅ All good! Database ready.");
            } else {
                System.out.println("❌ Connection failed");
            }

        } catch (Exception e) {
            System.err.println("❌ Setup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.shutdown();
        }
    }
}
