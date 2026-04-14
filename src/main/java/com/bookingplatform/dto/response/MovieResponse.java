package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private String language;
    private String genre;
    private Integer durationMinutes;
    private String rating;
}
