package org.nosulkora.fileloader.repository.impl;

import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.repository.FileRepository;
import org.nosulkora.fileloader.utils.SessionManager;

import java.util.List;

public class FileRepositoryImpl implements FileRepository {
    @Override
    public File save(File file) {
        return SessionManager.execute(session -> session.merge(file));
    }

    @Override
    public File update(File file) {
        return save(file);
    }

    @Override
    public File getById(Integer id) {
        return SessionManager.executeReadOnly(session -> session.get(File.class, id));
    }

    @Override
    public List<File> getAll() {
        return SessionManager.executeReadOnly(session -> session
                .createQuery("FROM File", File.class)
                .getResultList());
    }

    @Override
    public boolean deleteById(Integer id) {
        Boolean result = SessionManager.execute(session -> {
            File file = session.get(File.class, id);
            if (file != null) {
                session.remove(id);
                return true;
            }
            return false;
        });
        return Boolean.TRUE.equals(result);
    }
}
