package ru.practicum.workshop.eventservice.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.params.EventSearchParam;

import java.util.ArrayList;
import java.util.List;

public class CustomizedEventRepositoryImpl implements CustomizedEventRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final Sort defaultSort = Sort.by(Sort.Direction.DESC, "createdDateTime");

    @Override
    public List<Event> getEvents(EventSearchParam param) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Pageable pageable = param.getPageable();
        query.select(root)
                .where(buildPredicate(criteriaBuilder, root, param))
                .orderBy(QueryUtils.toOrders(pageable.getSortOr(defaultSort), root, criteriaBuilder));
        int pageNumber = (pageable.getPageNumber() == 0) ? 0 : pageable.getPageSize() * pageable.getPageNumber();

        return entityManager.createQuery(query)
                .setFirstResult(pageNumber)
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private Predicate[] buildPredicate(CriteriaBuilder cb,
                                       Root<Event> root,
                                       EventSearchParam param) {
        List<Predicate> predicates = new ArrayList<>();

        if (param.getStatus() != null) {
            predicates.add(cb.in(root.get("registrationStatus")).value(param.getStatus()));
        }
        if (param.getOwnerId() != null) {
            predicates.add(cb.equal(root.get("ownerId"), param.getOwnerId()));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
