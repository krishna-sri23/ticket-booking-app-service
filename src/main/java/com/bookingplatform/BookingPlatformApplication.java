package com.bookingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BookingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingPlatformApplication.class, args);
    }
}
