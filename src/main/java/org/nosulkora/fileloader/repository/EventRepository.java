package org.nosulkora.fileloader.repository;

import org.nosulkora.fileloader.entity.Event;

public interface EventRepository extends GenericRepository<Event, Integer> {
    Event findLatestByFileId(Integer fileId);
}
