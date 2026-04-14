package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BrowseShowsResponse {
    private MovieResponse movie;
    private Long cityId;
    private String cityName;
    private LocalDate date;
    private List<TheatreShowsResponse> theatres;
}
