package org.nosulkora.fileloader;

import org.nosulkora.fileloader.database.DatabaseManager;
import org.nosulkora.fileloader.database.FlywayManager;

public class AppRunner {

    public static void main(String[] args) {

        try {
            // Выполняем миграции
            FlywayManager.runMigrations();

            // Проверяем доступность базы данных
            if (!DatabaseManager.isDatabaseAvailable()) {
                throw new RuntimeException("База данных недоступна");
            }
            // Запускаем Tomcat
            TomcatStarter.startTomcat();

        } catch (Exception e) {
            System.out.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
