package com.bookingplatform.service;

import com.bookingplatform.dto.request.AdminShowRequest;
import com.bookingplatform.dto.response.ShowAdminResponse;
import com.bookingplatform.entity.*;
import com.bookingplatform.enums.BookingStatus;
import com.bookingplatform.enums.SeatType;
import com.bookingplatform.enums.ShowSeatStatus;
import com.bookingplatform.enums.ShowStatus;
import com.bookingplatform.exception.BookingException;
import com.bookingplatform.exception.ResourceNotFoundException;
import com.bookingplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminShowService {

    private static final BigDecimal REGULAR_MULT = new BigDecimal("1.0");
    private static final BigDecimal PREMIUM_MULT = new BigDecimal("1.5");
    private static final BigDecimal VIP_MULT = new BigDecimal("2.0");

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ShowAdminResponse createShow(AdminShowRequest req) {
        validateTimes(req);

        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + req.getMovieId()));
        Screen screen = screenRepository.findById(req.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + req.getScreenId()));

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(req.getShowDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .basePrice(req.getBasePrice())
                .status(ShowStatus.SCHEDULED)
                .build();
        Show saved = showRepository.save(show);

        // Auto-provision show_seat rows for every seat in the screen
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        if (seats.isEmpty()) {
            throw new BookingException("Screen " + screen.getId() + " has no seats configured");
        }
        for (Seat seat : seats) {
            ShowSeat ss = ShowSeat.builder()
                    .show(saved)
                    .seat(seat)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(priceFor(req.getBasePrice(), seat.getSeatType()))
                    .version(0)
                    .build();
            showSeatRepository.save(ss);
        }
        log.info("Admin created show {} with {} seats", saved.getId(), seats.size());
        return toResponse(saved, seats.size());
    }

    @Transactional
    public ShowAdminResponse updateShow(Long showId, AdminShowRequest req) {
        validateTimes(req);
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        long bookings = bookingRepository.countByShowIdAndStatusIn(showId,
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING));
        boolean priceChanged = show.getBasePrice().compareTo(req.getBasePrice()) != 0;
        boolean screenChanged = !show.getScreen().getId().equals(req.getScreenId());
        if (bookings > 0 && (priceChanged || screenChanged)) {
            throw new BookingException("Cannot change price or screen after bookings exist (bookings=" + bookings + ")");
        }

        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + req.getMovieId()));
        Screen screen = screenRepository.findById(req.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + req.getScreenId()));

        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(req.getShowDate());
        show.setStartTime(req.getStartTime());
        show.setEndTime(req.getEndTime());
        show.setBasePrice(req.getBasePrice());
        Show saved = showRepository.save(show);

        int seatCount = seatRepository.findByScreenId(screen.getId()).size();
        log.info("Admin updated show {}", saved.getId());
        return toResponse(saved, seatCount);
    }

    @Transactional
    public void deleteShow(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        long bookings = bookingRepository.countByShowIdAndStatusIn(showId,
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING));
        if (bookings > 0) {
            throw new BookingException("Cannot delete show with active bookings (bookings=" + bookings + ")");
        }
        showSeatRepository.deleteByShowId(showId);
        showRepository.delete(show);
        log.info("Admin deleted show {}", showId);
    }

    private void validateTimes(AdminShowRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new BookingException("endTime must be after startTime");
        }
    }

    private BigDecimal priceFor(BigDecimal base, SeatType type) {
        BigDecimal mult = switch (type) {
            case REGULAR -> REGULAR_MULT;
            case PREMIUM -> PREMIUM_MULT;
            case VIP -> VIP_MULT;
        };
        return base.multiply(mult);
    }

    private ShowAdminResponse toResponse(Show s, int totalSeats) {
        return ShowAdminResponse.builder()
                .showId(s.getId())
                .movieId(s.getMovie().getId())
                .movieTitle(s.getMovie().getTitle())
                .screenId(s.getScreen().getId())
                .screenName(s.getScreen().getName())
                .theatreName(s.getScreen().getTheatre().getName())
                .showDate(s.getShowDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .basePrice(s.getBasePrice())
                .status(s.getStatus().name())
                .totalSeats(totalSeats)
                .build();
    }
}
