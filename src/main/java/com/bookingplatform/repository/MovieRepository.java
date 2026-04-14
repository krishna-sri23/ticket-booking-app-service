package com.bookingplatform.repository;

import com.bookingplatform.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByLanguageIgnoreCase(String language);
    List<Movie> findByGenreIgnoreCase(String genre);
}
