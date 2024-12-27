package ru.practicum.workshop.eventservice.repository;

import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.params.EventSearchParam;

import java.util.List;

public interface CustomizedEventRepository {
    List<Event> getEvents(EventSearchParam param);
}
