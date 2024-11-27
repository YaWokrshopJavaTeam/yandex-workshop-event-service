package ru.practicum.workshop.eventservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizing_team_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrgTeamMember {

    public enum Role {
        EXECUTOR,
        MANAGER;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @ToString.Include(name = "eventId")
    private Long getEventId() {
        return event != null ? event.getId() : null;
    }

}

