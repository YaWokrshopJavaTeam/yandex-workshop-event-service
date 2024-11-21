package ru.practicum.workshop.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.workshop.eventservice.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByOwnerId(Long ownerId, Pageable pageable);
}
