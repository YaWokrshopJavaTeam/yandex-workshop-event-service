package ru.practicum.workshop.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.params.EventSearchParam;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, CustomizedEventRepository {
    List<Event> getEvents(EventSearchParam param);
}
