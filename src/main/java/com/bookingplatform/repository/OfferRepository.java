package com.bookingplatform.repository;

import com.bookingplatform.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("""
           SELECT DISTINCT o FROM Offer o
           LEFT JOIN FETCH o.rules
           WHERE o.active = true
             AND (o.validFrom IS NULL OR o.validFrom <= :today)
             AND (o.validUntil IS NULL OR o.validUntil >= :today)
           ORDER BY o.priority ASC
           """)
    List<Offer> findActiveOffers(@Param("today") LocalDate today);
}
