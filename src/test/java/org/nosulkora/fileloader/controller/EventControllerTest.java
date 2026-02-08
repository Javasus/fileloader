package org.nosulkora.fileloader.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("EventControllerTest")
public class EventControllerTest {

    private static final Integer EVENT_ID = 3;

    private EventRepository eventRepository;
    private EventController eventController;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        eventController = new EventController(eventRepository);
    }

    private File getFile() {
        return new File(
                1,
                "text.txt",
                "C:/fileloader/resources/uploadfifle/text.txt"
        );
    }

    private User getUser() {
        return new User(2, "Ivan ivanov", new ArrayList<>());
    }

    @Test
    @DisplayName("Create event")
    void createEventTest() {
        Event expectedEvent = new Event(getUser(), getFile());
        when(eventRepository.save(any(Event.class))).thenReturn(expectedEvent);
        Event actualEvent = eventController.createEvent(getUser(), getFile());
        assertAll(
                () -> assertEquals(expectedEvent.getId(), actualEvent.getId(), "eventId"),
                () -> assertEquals(expectedEvent.getUser(), actualEvent.getUser(), "userEvent"),
                () -> assertEquals(expectedEvent.getFile(), actualEvent.getFile(), "fileEvent")
        );
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Update event")
    void updateEventTest() {
        File newFile = new File(5, "picture.png", "C:/fileloader/resources/uploadfifle/picture.png");
        Event existingEvent = new Event(EVENT_ID, getUser(), getFile());
        Event updatedEvent = new Event(EVENT_ID, getUser(), newFile);
        when(eventRepository.getById(EVENT_ID)).thenReturn(existingEvent);
        when(eventRepository.update(any(Event.class))).thenReturn(updatedEvent);
        Event actualEvent = eventController.updateEvent(EVENT_ID, newFile);
        assertAll(
                () -> assertEquals(updatedEvent.getId(), actualEvent.getId(), "eventId"),
                () -> assertEquals(updatedEvent.getUser(), actualEvent.getUser(), "userEvent"),
                () -> assertEquals(updatedEvent.getFile(), actualEvent.getFile(), "fileEvent")
        );
        verify(eventRepository, times(1)).getById(EVENT_ID);
        verify(eventRepository, times(1)).update(any(Event.class));
    }

    @Test
    @DisplayName("Get event by ID")
    void getEventById() {
        Event expectedEvent = new Event(EVENT_ID, getUser(), getFile());
        when(eventRepository.getById(EVENT_ID)).thenReturn(expectedEvent);
        Event actualEvent = eventController.getEventById(EVENT_ID);
        assertAll(
                () -> assertEquals(expectedEvent.getId(), actualEvent.getId(), "eventId"),
                () -> assertEquals(expectedEvent.getUser(), actualEvent.getUser(), "userEvent"),
                () -> assertEquals(expectedEvent.getFile(), actualEvent.getFile(), "fileEvent")
        );
        verify(eventRepository, times(1)).getById(EVENT_ID);
    }

    @Test
    @DisplayName("Get all event")
    void fetAllEvent() {
        Event firstEvent = new Event(EVENT_ID, getUser(), getFile());
        Event secondEvent = new Event(
                5,
                new User(6, "Roman", new ArrayList<>()),
                new File(7, "homework.txt", "C:/fileloader/resources/uploadfifle/homework.txt.txt")
        );
        List<Event> expectedEvents = List.of(firstEvent, secondEvent);
        when(eventRepository.getAll()).thenReturn(expectedEvents);
        List<Event> actualEvents = eventController.getAllEvents();
        assertThat(expectedEvents)
                .describedAs("Get all events")
                .containsExactlyInAnyOrderElementsOf(actualEvents);
        verify(eventRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Update event but event is not found - return null")
    void updateEventNotFoundTest() {
        when(eventRepository.getById(EVENT_ID)).thenReturn(null);
        Event actualEvent = eventController.updateEvent(EVENT_ID, getFile());
        assertNull(actualEvent);
        verify(eventRepository, times(1)).getById(EVENT_ID);
        verify(eventRepository, never()).update(any(Event.class));
    }


}
