package org.nosulkora.fileloader.controller;

import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.EventRepository;
import org.nosulkora.fileloader.repository.impl.EventRepositoryImpl;

import java.util.List;
import java.util.Objects;

public class EventController {

    private final EventRepository eventRepository;

    public EventController() {
        this.eventRepository = new EventRepositoryImpl();
    }

    public Event createEvent(User user, File file) {
            Event event = new Event(user, file);
            return eventRepository.save(event);
    }

    public Event updateEvent(Integer id, File file) {
        Event eventById = eventRepository.getById(id);
        if (Objects.nonNull(eventById)) {
            eventById.setFile(file);
            return eventRepository.save(eventById);
        }
        return null;
    }

    public Event getEventById(Integer id) {
        return eventRepository.getById(id);
    }

    public List<Event> getAllEvents() {
        return eventRepository.getAll();
    }

    public boolean deleteEventById(Integer id) {
        return eventRepository.deleteById(id);
    }

    public Event findLatestByFileId(Integer fileId) {
        return eventRepository.findLatestByFileId(fileId);
    }
}
