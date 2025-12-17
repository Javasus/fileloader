package org.nosulkora.fileloader.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.entity.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseManager {
    private static final SessionFactory sessionFactory;

    static {
        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(getHibernateProperties())
                    .build();

            MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(User.class);
            sources.addAnnotatedClass(File.class);
            sources.addAnnotatedClass(Event.class);

            sessionFactory = sources.buildMetadata().buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println("Ошибка инициализации SessionFactory." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Получить SessionFactory Hibernate
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("Hibernate SessionFactory закрыта!");
        }
    }

    public static boolean isDatabaseAvailable() {
        try (Session session = sessionFactory.openSession()) {
            // Простой запрос для проверки соединения
            session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Properties getHibernateProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader()
                .getResourceAsStream("hibernate.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load hibernate.properties", e);
        }
        return properties;
    }
}
