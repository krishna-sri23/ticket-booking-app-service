package com.bookingplatform.repository;

import com.bookingplatform.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    long countByShowIdAndStatusIn(Long showId, List<com.bookingplatform.enums.BookingStatus> statuses);
}
