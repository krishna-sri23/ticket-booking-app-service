package com.bookingplatform.service;

import com.bookingplatform.dto.response.BrowseShowsResponse;
import com.bookingplatform.dto.response.MovieResponse;
import com.bookingplatform.dto.response.ShowTimingResponse;
import com.bookingplatform.dto.response.TheatreShowsResponse;
import com.bookingplatform.entity.Movie;
import com.bookingplatform.entity.Show;
import com.bookingplatform.entity.ShowSeat;
import com.bookingplatform.entity.Theatre;
import com.bookingplatform.enums.ShowSeatStatus;
import com.bookingplatform.exception.ResourceNotFoundException;
import com.bookingplatform.repository.CityRepository;
import com.bookingplatform.repository.MovieRepository;
import com.bookingplatform.repository.ShowRepository;
import com.bookingplatform.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * READ scenario: Browse theatres currently running the show (movie selected)
 * in the town, including show timings by a chosen date.
 */
@Service
@RequiredArgsConstructor
public class ShowBrowseService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public BrowseShowsResponse browseShows(Long movieId, Long cityId, LocalDate date) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + movieId));
        var city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));

        List<Show> shows = showRepository.findShowsByMovieCityAndDate(movieId, cityId, date);

        // Group by theatre preserving order
        Map<Long, TheatreShowsResponse.TheatreShowsResponseBuilder> theatreMap = new LinkedHashMap<>();
        Map<Long, List<ShowTimingResponse>> timingMap = new LinkedHashMap<>();

        for (Show show : shows) {
            Theatre theatre = show.getScreen().getTheatre();
            Long tId = theatre.getId();
            theatreMap.computeIfAbsent(tId, k -> TheatreShowsResponse.builder()
                    .theatreId(theatre.getId())
                    .theatreName(theatre.getName())
                    .address(theatre.getAddress())
                    .cityName(theatre.getCity().getName()));
            timingMap.computeIfAbsent(tId, k -> new ArrayList<>());

            int available = (int) showSeatRepository
                    .findByShowIdAndStatus(show.getId(), ShowSeatStatus.AVAILABLE).size();

            timingMap.get(tId).add(ShowTimingResponse.builder()
                    .showId(show.getId())
                    .startTime(show.getStartTime())
                    .endTime(show.getEndTime())
                    .basePrice(show.getBasePrice())
                    .screenName(show.getScreen().getName())
                    .availableSeats(available)
                    .build());
        }

        List<TheatreShowsResponse> theatreList = new ArrayList<>();
        theatreMap.forEach((tId, builder) ->
                theatreList.add(builder.shows(timingMap.get(tId)).build()));

        return BrowseShowsResponse.builder()
                .movie(MovieResponse.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .language(movie.getLanguage())
                        .genre(movie.getGenre())
                        .durationMinutes(movie.getDurationMinutes())
                        .rating(movie.getRating())
                        .build())
                .cityId(city.getId())
                .cityName(city.getName())
                .date(date)
                .theatres(theatreList)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ShowSeat> getShowSeats(Long showId) {
        return showSeatRepository.findByShowId(showId);
    }
}
