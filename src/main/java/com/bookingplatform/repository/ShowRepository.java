package com.bookingplatform.repository;

import com.bookingplatform.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
           SELECT s FROM Show s
           JOIN FETCH s.screen sc
           JOIN FETCH sc.theatre t
           JOIN FETCH t.city c
           JOIN FETCH s.movie m
           WHERE m.id = :movieId
             AND s.showDate = :showDate
             AND c.id = :cityId
             AND s.status = 'SCHEDULED'
           ORDER BY t.id, s.startTime
           """)
    List<Show> findShowsByMovieCityAndDate(@Param("movieId") Long movieId,
                                           @Param("cityId") Long cityId,
                                           @Param("showDate") LocalDate showDate);
}
