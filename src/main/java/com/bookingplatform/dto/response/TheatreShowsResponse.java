package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TheatreShowsResponse {
    private Long theatreId;
    private String theatreName;
    private String address;
    private String cityName;
    private List<ShowTimingResponse> shows;
}
