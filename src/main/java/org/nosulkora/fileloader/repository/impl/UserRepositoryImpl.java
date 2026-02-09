package org.nosulkora.fileloader.repository.impl;

import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.UserRepository;
import org.nosulkora.fileloader.utils.SessionManager;

import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    @Override
    public User save(User user) {
        return SessionManager.execute(session -> session.merge(user));
    }

    @Override
    public User update(User user) {
        return save(user);
    }

    @Override
    public User getById(Integer id) {
        return SessionManager.executeReadOnly(session -> session.get(User.class, id));
    }

    @Override
    public List<User> getAll() {
        return SessionManager
                .executeReadOnly(session -> session.createQuery("FROM User", User.class)
                        .getResultList());
    }

    @Override
    public boolean deleteById(Integer id) {
        Boolean result = SessionManager.execute(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                return true;
            }
            return false;
        });
        return Boolean.TRUE.equals(result);
    }
}
