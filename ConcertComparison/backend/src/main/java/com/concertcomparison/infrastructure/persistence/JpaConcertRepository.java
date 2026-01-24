package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.repository.ConcertFilterCriteria;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.infrastructure.persistence.specification.ConcertSpecifications;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA Implementierung des ConcertRepository.
 * 
 * Infrastructure Layer: Implementiert das Domain Repository Interface.
 * Verwendet Spring Data JPA für automatische Query-Generierung.
 */
@Repository
public interface JpaConcertRepository extends JpaRepository<Concert, Long>, JpaSpecificationExecutor<Concert>, ConcertRepository {
    
    // Spring Data JPA generiert automatisch Queries basierend auf Methodennamen
    
    @Override
    List<Concert> findByNameContainingIgnoreCase(String name);
    
    @Override
    List<Concert> findByVenueContainingIgnoreCase(String venue);
    
    @Override
    List<Concert> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Custom Query für zukünftige Concerts.
     */
    @Query("SELECT c FROM Concert c WHERE c.date > CURRENT_TIMESTAMP ORDER BY c.date ASC")
    @Override
    List<Concert> findFutureConcerts();
    
    /**
     * Custom Query für vergangene Concerts.
     */
    @Query("SELECT c FROM Concert c WHERE c.date < CURRENT_TIMESTAMP ORDER BY c.date DESC")
    @Override
    List<Concert> findPastConcerts();

    @Override
    default Page<Concert> findAllWithFilters(ConcertFilterCriteria filter, Pageable pageable) {
        Sort resolvedSort = resolveSort(pageable.getSort());
        Sort effectiveSort = resolvedSort == null ? Sort.unsorted() : resolvedSort;

        if (pageable.isUnpaged()) {
            List<Concert> content = findAll(Objects.requireNonNull(ConcertSpecifications.from(filter)), effectiveSort);
            return new PageImpl<>(content);
        }

        PageRequest effectivePageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            effectiveSort
        );

        return findAll(Objects.requireNonNull(ConcertSpecifications.from(filter)), effectivePageable);
    }

    private static Sort resolveSort(Sort sort) {
        Sort incoming = sort.isSorted() ? sort : Sort.by(Sort.Order.asc("date"));
        Sort resolved = Sort.unsorted();

        for (Sort.Order order : incoming) {
            if ("price".equalsIgnoreCase(order.getProperty())) {
                resolved = resolved.and(JpaSort.unsafe(
                    order.getDirection(),
                    "(SELECT MIN(s.price) FROM seats s WHERE s.concert_id = concerts.id)"
                ));
            } else if ("name".equalsIgnoreCase(order.getProperty())) {
                resolved = resolved.and(Sort.by(order.withProperty("name")));
            } else {
                resolved = resolved.and(Sort.by(order.withProperty("date")));
            }
        }

        return resolved.isSorted() ? resolved : Sort.by(Sort.Order.asc("date"));
    }
}
