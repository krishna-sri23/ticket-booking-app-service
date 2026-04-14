package com.bookingplatform.controller;

import com.bookingplatform.dto.request.BookingRequest;
import com.bookingplatform.dto.response.BookingResponse;
import com.bookingplatform.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings", description = "Create and retrieve bookings")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Book tickets (with automatic discount application)")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get booking details by ID")
    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId) {
        return bookingService.getBooking(bookingId);
    }
}
