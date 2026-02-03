package org.nosulkora.fileloader.repository.impl;

import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.repository.EventRepository;
import org.nosulkora.fileloader.utils.SessionManager;

import java.util.List;

public class EventRepositoryImpl implements EventRepository {
    @Override
    public Event save(Event event) {
        return SessionManager.execute(session -> session.merge(event));
    }

    @Override
    public Event update(Event event) {
        return save(event);
    }

    @Override
    public Event getById(Integer id) {
        return SessionManager.executeReadOnly(session -> session.get(Event.class, id));
    }

    @Override
    public List<Event> getAll() {
        return SessionManager.executeReadOnly(session -> session
                .createQuery("FROM Event", Event.class)
                .getResultList());
    }

    @Override
    public boolean deleteById(Integer id) {
        Boolean result = SessionManager.execute(session -> {
            Event event = session.get(Event.class, id);
            if (event != null) {
                session.remove(event);
                return true;
            }
            return false;
        });
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Event findLatestByFileId(Integer fileId) {
        return SessionManager.executeReadOnly(session ->
                session.createQuery("FROM Event WHERE file.id = :fileId ORDER BY id DESC", Event.class)
                        .setParameter("fileId", fileId)
                        .setMaxResults(1)
                        .uniqueResult()
        );
    }
}
