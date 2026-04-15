package com.bookingplatform.controller;

import com.bookingplatform.dto.request.AdminShowRequest;
import com.bookingplatform.dto.response.ShowAdminResponse;
import com.bookingplatform.service.AdminShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Shows", description = "Theatre admin CRUD for shows")
@RestController
@RequestMapping("/api/v1/admin/shows")
@RequiredArgsConstructor
public class AdminShowController {

    private final AdminShowService adminShowService;

    @Operation(summary = "Create a show and auto-provision seat inventory")
    @PostMapping
    public ResponseEntity<ShowAdminResponse> create(@Valid @RequestBody AdminShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminShowService.createShow(request));
    }

    @Operation(summary = "Update show details")
    @PutMapping("/{showId}")
    public ShowAdminResponse update(@PathVariable Long showId,
                                    @Valid @RequestBody AdminShowRequest request) {
        return adminShowService.updateShow(showId, request);
    }

    @Operation(summary = "Delete show (only when there are no active bookings)")
    @DeleteMapping("/{showId}")
    public ResponseEntity<Void> delete(@PathVariable Long showId) {
        adminShowService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }
}
