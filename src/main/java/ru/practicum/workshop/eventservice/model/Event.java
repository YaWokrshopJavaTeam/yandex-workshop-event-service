package ru.practicum.workshop.eventservice.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.workshop.eventservice.validation.ValidDateRange;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "owner_id", nullable = false)
    public Long ownerId;

    public Long getOwnerId(){
        return ownerId;
    }
}