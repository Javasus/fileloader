package org.nosulkora.fileloader.utils;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nosulkora.fileloader.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Function;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private SessionManager() {
    }

    public static <T> T execute(Function<Session, T> operation) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T result = operation.apply(session);
            if (result == null) {
                throw new SQLException("Ошибка при выполнении транзакции");
            }
            transaction.commit();
            logger.debug("Транзакция завершена успешно");
            return result;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.warn("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Ошибка выполнения транзакции", e);
            return null;
        }
    }

    /**
     * Для операций ТОЛЬКО чтения (SELECT).
     * Не открывает транзакцию для производительности.
     */
    public static <T> T executeReadOnly(Function<Session, T> operation) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            T result = operation.apply(session);
            if (result == null) {
                throw new SQLException("Ошибка при выполнении запроса без открытия транзакции.");
            }
            logger.debug("Запрос завершен успешно");
            return result;
        } catch (Exception e) {
            logger.warn("Ошибка выполнения операции только для чтения", e);
            return null;
        }
    }
}
