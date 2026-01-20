package org.nosulkora.fileloader.database;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nosulkora.fileloader.entity.User;

public class SimpleHibernateTest {
    public static void main(String[] args) {
        System.out.println("🎯 Простой тест Hibernate");

        Session session = null;
        try {
            session = DatabaseManager.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            // Тестовый текст
            String testText = "Hibernate тест: Привет! " + System.currentTimeMillis();
            System.out.println("📝 Тестовый текст: " + testText);

            // Сохраняем
            User user = new User();
            user.setName(testText);
            session.persist(user);
            tx.commit();

            System.out.println("✅ Сохранено, ID: " + user.getId());

            // Сразу читаем
            session.clear(); // Очищаем кэш
            User savedUser = session.find(User.class, user.getId());
            System.out.println("📖 Прочитано через Hibernate: " + savedUser.getName());

            // Читаем через нативный SQL
            String nativeResult = (String) session.createNativeQuery(
                            "SELECT name FROM users WHERE id = :id", String.class)
                    .setParameter("id", user.getId())
                    .getSingleResult();

            System.out.println("🔍 Прочитано через нативный SQL: " + nativeResult);

            // Сравниваем
            System.out.println("✅ Совпадает? " + testText.equals(nativeResult));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }
}
