package com.bookingplatform.controller;

import com.bookingplatform.dto.response.BrowseShowsResponse;
import com.bookingplatform.dto.response.ShowSeatResponse;
import com.bookingplatform.service.ShowBrowseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Shows", description = "Browse movies, theatres, and shows")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShowController {

    private final ShowBrowseService browseService;

    @Operation(summary = "Browse theatres & show timings for a movie in a city on a date")
    @GetMapping("/movies/{movieId}/shows")
    public BrowseShowsResponse browseShows(
            @PathVariable Long movieId,
            @RequestParam Long cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return browseService.browseShows(movieId, cityId, date);
    }

    @Operation(summary = "Get seat availability for a show")
    @GetMapping("/shows/{showId}/seats")
    public List<ShowSeatResponse> getShowSeats(@PathVariable Long showId) {
        return browseService.getShowSeats(showId).stream()
                .map(ss -> ShowSeatResponse.builder()
                        .showSeatId(ss.getId())
                        .seatNumber(ss.getSeat().getSeatNumber())
                        .rowLabel(ss.getSeat().getRowLabel())
                        .seatType(ss.getSeat().getSeatType().name())
                        .status(ss.getStatus().name())
                        .price(ss.getPrice())
                        .build())
                .toList();
    }
}
