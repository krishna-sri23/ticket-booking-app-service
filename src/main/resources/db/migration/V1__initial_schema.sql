-- =====================================================
-- V1: Initial schema for Movie Ticket Booking Platform
-- =====================================================

CREATE TABLE city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_city_name_state (name, state)
);

CREATE TABLE theatre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    city_id BIGINT NOT NULL,
    total_screens INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_theatre_city FOREIGN KEY (city_id) REFERENCES city(id),
    INDEX idx_theatre_city (city_id)
);

CREATE TABLE screen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theatre_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_screen_theatre FOREIGN KEY (theatre_id) REFERENCES theatre(id),
    INDEX idx_screen_theatre (theatre_id)
);

CREATE TABLE seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    row_label VARCHAR(5) NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_screen FOREIGN KEY (screen_id) REFERENCES screen(id),
    UNIQUE KEY uq_seat_screen_number (screen_id, seat_number)
);

CREATE TABLE movie (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    language VARCHAR(50) NOT NULL,
    genre VARCHAR(50),
    duration_minutes INT NOT NULL,
    release_date DATE,
    rating VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movie_language (language),
    INDEX idx_movie_genre (genre)
);

CREATE TABLE shows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    screen_id BIGINT NOT NULL,
    show_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movie(id),
    CONSTRAINT fk_show_screen FOREIGN KEY (screen_id) REFERENCES screen(id),
    INDEX idx_show_movie_date (movie_id, show_date),
    INDEX idx_show_screen_date (screen_id, show_date)
);

CREATE TABLE show_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price DECIMAL(10,2) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ss_show FOREIGN KEY (show_id) REFERENCES shows(id),
    CONSTRAINT fk_ss_seat FOREIGN KEY (seat_id) REFERENCES seat(id),
    UNIQUE KEY uq_show_seat (show_id, seat_id),
    INDEX idx_ss_show_status (show_id, status)
);

CREATE TABLE platform_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES platform_user(id),
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(id),
    INDEX idx_booking_user (user_id),
    INDEX idx_booking_show (show_id)
);

CREATE TABLE booking_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    show_seat_id BIGINT NOT NULL,
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_bs_showseat FOREIGN KEY (show_seat_id) REFERENCES show_seat(id),
    UNIQUE KEY uq_booking_showseat (show_seat_id)
);

CREATE TABLE offer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    applies_to VARCHAR(20) NOT NULL,
    nth_ticket_number INT,
    priority INT NOT NULL DEFAULT 100,
    stackable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_offer_active (active, valid_from, valid_until)
);

CREATE TABLE offer_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    rule_type VARCHAR(30) NOT NULL,
    rule_value VARCHAR(1000) NOT NULL,
    CONSTRAINT fk_orule_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE,
    INDEX idx_orule_offer (offer_id)
);

CREATE TABLE offer_city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    CONSTRAINT fk_ocity_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE,
    CONSTRAINT fk_ocity_city FOREIGN KEY (city_id) REFERENCES city(id),
    UNIQUE KEY uq_offer_city (offer_id, city_id)
);

CREATE TABLE offer_theatre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    theatre_id BIGINT NOT NULL,
    CONSTRAINT fk_otheatre_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE,
    CONSTRAINT fk_otheatre_theatre FOREIGN KEY (theatre_id) REFERENCES theatre(id),
    UNIQUE KEY uq_offer_theatre (offer_id, theatre_id)
);

CREATE TABLE booking_offer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    offer_id BIGINT NOT NULL,
    discount_applied DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_bo_booking FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    CONSTRAINT fk_bo_offer FOREIGN KEY (offer_id) REFERENCES offer(id),
    INDEX idx_bo_booking (booking_id)
);
