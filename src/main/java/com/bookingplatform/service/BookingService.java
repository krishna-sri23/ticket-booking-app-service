package com.bookingplatform.service;

import com.bookingplatform.dto.request.BookingRequest;
import com.bookingplatform.dto.response.AppliedOfferResponse;
import com.bookingplatform.dto.response.BookingResponse;
import com.bookingplatform.entity.*;
import com.bookingplatform.enums.BookingStatus;
import com.bookingplatform.enums.ShowSeatStatus;
import com.bookingplatform.exception.ResourceNotFoundException;
import com.bookingplatform.exception.SeatUnavailableException;
import com.bookingplatform.repository.BookingRepository;
import com.bookingplatform.repository.ShowSeatRepository;
import com.bookingplatform.repository.UserRepository;
import com.bookingplatform.service.discount.AppliedOffer;
import com.bookingplatform.service.discount.DiscountContext;
import com.bookingplatform.service.discount.DiscountEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WRITE scenario: Book movie tickets by selecting a theatre, timing, and preferred seats.
 *
 * Transaction notes:
 * - Uses pessimistic locking on show_seat rows to prevent concurrent double-booking.
 * - Applies all configured offers via DiscountEngine.
 * - Atomic: booking + booking_seats + booking_offers + show_seat status update = one transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DiscountEngine discountEngine;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse createBooking(BookingRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));

        // Pessimistic lock on selected show_seats to avoid race conditions
        List<ShowSeat> seats = showSeatRepository.findAllByIdForUpdate(req.getShowSeatIds());

        if (seats.size() != req.getShowSeatIds().size()) {
            throw new ResourceNotFoundException("One or more show seats not found");
        }

        // All seats must be AVAILABLE and belong to the same show
        for (ShowSeat ss : seats) {
            if (ss.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Seat " + ss.getSeat().getSeatNumber()
                        + " is not available (status=" + ss.getStatus() + ")");
            }
            if (!ss.getShow().getId().equals(req.getShowId())) {
                throw new SeatUnavailableException("Seat " + ss.getSeat().getSeatNumber()
                        + " does not belong to show " + req.getShowId());
            }
        }

        Show show = seats.get(0).getShow();
        Theatre theatre = show.getScreen().getTheatre();

        BigDecimal total = seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate discounts
        DiscountContext ctx = DiscountContext.builder()
                .show(show)
                .theatre(theatre)
                .cityId(theatre.getCity().getId())
                .seats(seats)
                .build();
        List<AppliedOffer> applied = discountEngine.calculateDiscounts(ctx);

        BigDecimal totalDiscount = applied.stream()
                .map(AppliedOffer::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cap discount at total
        if (totalDiscount.compareTo(total) > 0) totalDiscount = total;
        BigDecimal finalAmount = total.subtract(totalDiscount);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalAmount(total)
                .discount(totalDiscount)
                .finalAmount(finalAmount)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build();

        // Attach booking_seats
        for (ShowSeat ss : seats) {
            booking.getBookingSeats().add(BookingSeat.builder()
                    .booking(booking)
                    .showSeat(ss)
                    .build());
            ss.setStatus(ShowSeatStatus.BOOKED);
        }

        // Attach applied offers
        for (AppliedOffer ao : applied) {
            booking.getBookingOffers().add(BookingOffer.builder()
                    .booking(booking)
                    .offer(ao.getOffer())
                    .discountApplied(ao.getDiscountAmount())
                    .build());
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created for user {} with {} seats, discount={}, final={}",
                saved.getId(), user.getId(), seats.size(), totalDiscount, finalAmount);

        return toResponse(saved, seats, applied);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        List<ShowSeat> seats = booking.getBookingSeats().stream()
                .map(BookingSeat::getShowSeat).toList();
        List<AppliedOffer> applied = booking.getBookingOffers().stream()
                .map(bo -> new AppliedOffer(bo.getOffer(), bo.getDiscountApplied()))
                .toList();
        return toResponse(booking, seats, applied);
    }

    private BookingResponse toResponse(Booking booking, List<ShowSeat> seats, List<AppliedOffer> applied) {
        List<String> seatLabels = seats.stream()
                .map(s -> s.getSeat().getRowLabel() + s.getSeat().getSeatNumber())
                .collect(Collectors.toList());

        List<AppliedOfferResponse> offers = applied.stream()
                .map(a -> AppliedOfferResponse.builder()
                        .code(a.getOffer().getCode())
                        .name(a.getOffer().getName())
                        .discountApplied(a.getDiscountAmount())
                        .build())
                .collect(Collectors.toList());

        Show show = booking.getShow();
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUser().getId())
                .showId(show.getId())
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getScreen().getTheatre().getName())
                .screenName(show.getScreen().getName())
                .bookedSeats(seatLabels)
                .totalAmount(booking.getTotalAmount())
                .discount(booking.getDiscount())
                .finalAmount(booking.getFinalAmount())
                .appliedOffers(offers)
                .status(booking.getStatus().name())
                .bookedAt(booking.getBookedAt())
                .build();
    }
}
