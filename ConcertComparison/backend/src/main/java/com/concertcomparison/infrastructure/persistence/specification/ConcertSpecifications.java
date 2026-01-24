package com.concertcomparison.infrastructure.persistence.specification;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertFilterCriteria;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Baut dynamische JPA Specifications für Concert-Filter.
 */
public final class ConcertSpecifications {

    private ConcertSpecifications() {}

    public static Specification<Concert> from(ConcertFilterCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.date() != null) {
                LocalDateTime startOfDay = criteria.date().atStartOfDay();
                LocalDateTime endOfDay = criteria.date().atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("date"), startOfDay, endOfDay));
            }

            if (criteria.venue() != null && !criteria.venue().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("venue")), "%" + criteria.venue().toLowerCase() + "%"));
            }

            if (criteria.minPrice() != null) {
                predicates.add(buildPricePredicate(query, cb, root.get("id"), criteria.minPrice(), true));
            }

            if (criteria.maxPrice() != null) {
                predicates.add(buildPricePredicate(query, cb, root.get("id"), criteria.maxPrice(), false));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildPricePredicate(jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                 jakarta.persistence.criteria.CriteriaBuilder cb,
                                                 jakarta.persistence.criteria.Path<Long> concertIdPath,
                                                 Double price,
                                                 boolean isMin) {
        Subquery<Double> priceSubquery = query.subquery(Double.class);
        var seatRoot = priceSubquery.from(Seat.class);
        priceSubquery.select(cb.min(seatRoot.get("price")));
        priceSubquery.where(cb.equal(seatRoot.get("concertId"), concertIdPath));
        return isMin
            ? cb.greaterThanOrEqualTo(priceSubquery, price)
            : cb.lessThanOrEqualTo(priceSubquery, price);
    }
}
