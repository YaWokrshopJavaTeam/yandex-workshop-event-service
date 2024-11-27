package ru.practicum.workshop.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;

import java.util.List;
import java.util.Optional;

public interface OrgTeamMemberRepository extends JpaRepository<OrgTeamMember, Long> {

    Optional<OrgTeamMember> findByEventIdAndUserId(long eventId, long userId);

    Optional<OrgTeamMember> findByEventIdAndUserIdAndRole(long eventId, long userId, OrgTeamMember.Role role);

    List<OrgTeamMember> findAllByEventId(long eventId);

}
