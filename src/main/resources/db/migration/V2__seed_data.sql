-- =====================================================
-- V2: Seed sample data
-- =====================================================

-- Cities
INSERT INTO city (id, name, state, country) VALUES
(1, 'Mumbai', 'Maharashtra', 'India'),
(2, 'Bangalore', 'Karnataka', 'India'),
(3, 'Delhi', 'Delhi', 'India');

-- Theatres
INSERT INTO theatre (id, name, address, city_id, total_screens, status) VALUES
(1, 'PVR Phoenix', 'Phoenix Mall, Lower Parel', 1, 2, 'ACTIVE'),
(2, 'INOX Nariman Point', 'Nariman Point', 1, 1, 'ACTIVE'),
(3, 'PVR Forum', 'Forum Mall, Koramangala', 2, 2, 'ACTIVE'),
(4, 'Cinepolis Saket', 'Select Citywalk, Saket', 3, 1, 'ACTIVE');

-- Screens
INSERT INTO screen (id, theatre_id, name, total_seats) VALUES
(1, 1, 'Screen 1', 20),
(2, 1, 'Screen 2', 20),
(3, 2, 'Screen 1', 15),
(4, 3, 'Screen 1', 20),
(5, 3, 'Screen 2', 20),
(6, 4, 'Screen 1', 15);

-- Seats (5 rows x 4 seats = 20 per screen; 3 rows x 5 = 15)
-- Screen 1 (20 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(1,'A1','A','REGULAR'),(1,'A2','A','REGULAR'),(1,'A3','A','REGULAR'),(1,'A4','A','REGULAR'),
(1,'B1','B','REGULAR'),(1,'B2','B','REGULAR'),(1,'B3','B','REGULAR'),(1,'B4','B','REGULAR'),
(1,'C1','C','PREMIUM'),(1,'C2','C','PREMIUM'),(1,'C3','C','PREMIUM'),(1,'C4','C','PREMIUM'),
(1,'D1','D','PREMIUM'),(1,'D2','D','PREMIUM'),(1,'D3','D','PREMIUM'),(1,'D4','D','PREMIUM'),
(1,'E1','E','VIP'),(1,'E2','E','VIP'),(1,'E3','E','VIP'),(1,'E4','E','VIP');

-- Screen 2 (20 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(2,'A1','A','REGULAR'),(2,'A2','A','REGULAR'),(2,'A3','A','REGULAR'),(2,'A4','A','REGULAR'),
(2,'B1','B','REGULAR'),(2,'B2','B','REGULAR'),(2,'B3','B','REGULAR'),(2,'B4','B','REGULAR'),
(2,'C1','C','PREMIUM'),(2,'C2','C','PREMIUM'),(2,'C3','C','PREMIUM'),(2,'C4','C','PREMIUM'),
(2,'D1','D','PREMIUM'),(2,'D2','D','PREMIUM'),(2,'D3','D','PREMIUM'),(2,'D4','D','PREMIUM'),
(2,'E1','E','VIP'),(2,'E2','E','VIP'),(2,'E3','E','VIP'),(2,'E4','E','VIP');

-- Screen 3 (15 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(3,'A1','A','REGULAR'),(3,'A2','A','REGULAR'),(3,'A3','A','REGULAR'),(3,'A4','A','REGULAR'),(3,'A5','A','REGULAR'),
(3,'B1','B','PREMIUM'),(3,'B2','B','PREMIUM'),(3,'B3','B','PREMIUM'),(3,'B4','B','PREMIUM'),(3,'B5','B','PREMIUM'),
(3,'C1','C','VIP'),(3,'C2','C','VIP'),(3,'C3','C','VIP'),(3,'C4','C','VIP'),(3,'C5','C','VIP');

-- Screen 4 (20 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(4,'A1','A','REGULAR'),(4,'A2','A','REGULAR'),(4,'A3','A','REGULAR'),(4,'A4','A','REGULAR'),
(4,'B1','B','REGULAR'),(4,'B2','B','REGULAR'),(4,'B3','B','REGULAR'),(4,'B4','B','REGULAR'),
(4,'C1','C','PREMIUM'),(4,'C2','C','PREMIUM'),(4,'C3','C','PREMIUM'),(4,'C4','C','PREMIUM'),
(4,'D1','D','PREMIUM'),(4,'D2','D','PREMIUM'),(4,'D3','D','PREMIUM'),(4,'D4','D','PREMIUM'),
(4,'E1','E','VIP'),(4,'E2','E','VIP'),(4,'E3','E','VIP'),(4,'E4','E','VIP');

-- Screen 5 (20 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(5,'A1','A','REGULAR'),(5,'A2','A','REGULAR'),(5,'A3','A','REGULAR'),(5,'A4','A','REGULAR'),
(5,'B1','B','REGULAR'),(5,'B2','B','REGULAR'),(5,'B3','B','REGULAR'),(5,'B4','B','REGULAR'),
(5,'C1','C','PREMIUM'),(5,'C2','C','PREMIUM'),(5,'C3','C','PREMIUM'),(5,'C4','C','PREMIUM'),
(5,'D1','D','PREMIUM'),(5,'D2','D','PREMIUM'),(5,'D3','D','PREMIUM'),(5,'D4','D','PREMIUM'),
(5,'E1','E','VIP'),(5,'E2','E','VIP'),(5,'E3','E','VIP'),(5,'E4','E','VIP');

-- Screen 6 (15 seats)
INSERT INTO seat (screen_id, seat_number, row_label, seat_type) VALUES
(6,'A1','A','REGULAR'),(6,'A2','A','REGULAR'),(6,'A3','A','REGULAR'),(6,'A4','A','REGULAR'),(6,'A5','A','REGULAR'),
(6,'B1','B','PREMIUM'),(6,'B2','B','PREMIUM'),(6,'B3','B','PREMIUM'),(6,'B4','B','PREMIUM'),(6,'B5','B','PREMIUM'),
(6,'C1','C','VIP'),(6,'C2','C','VIP'),(6,'C3','C','VIP'),(6,'C4','C','VIP'),(6,'C5','C','VIP');

-- Movies
INSERT INTO movie (id, title, language, genre, duration_minutes, release_date, rating) VALUES
(1, 'Inception', 'English', 'Sci-Fi', 148, '2024-01-01', 'PG-13'),
(2, 'Dangal', 'Hindi', 'Drama', 161, '2024-02-10', 'PG'),
(3, 'KGF Chapter 2', 'Kannada', 'Action', 168, '2024-03-15', 'UA');

-- Shows for 2026-04-15 (morning, afternoon, evening)
INSERT INTO shows (id, movie_id, screen_id, show_date, start_time, end_time, base_price, status) VALUES
(1, 1, 1, '2026-04-15', '10:00:00', '12:30:00', 200.00, 'SCHEDULED'),
(2, 1, 1, '2026-04-15', '14:00:00', '16:30:00', 200.00, 'SCHEDULED'),  -- afternoon
(3, 1, 1, '2026-04-15', '19:00:00', '21:30:00', 250.00, 'SCHEDULED'),
(4, 2, 2, '2026-04-15', '11:00:00', '13:45:00', 180.00, 'SCHEDULED'),
(5, 2, 2, '2026-04-15', '15:00:00', '17:45:00', 180.00, 'SCHEDULED'),  -- afternoon
(6, 1, 4, '2026-04-15', '13:00:00', '15:30:00', 220.00, 'SCHEDULED'),  -- afternoon, Bangalore
(7, 3, 5, '2026-04-15', '14:30:00', '17:20:00', 200.00, 'SCHEDULED');  -- afternoon, Bangalore

-- Show seats (generate AVAILABLE show_seat rows for each show using seats of its screen)
-- Using INSERT ... SELECT
INSERT INTO show_seat (show_id, seat_id, status, price, version)
SELECT s.id, st.id, 'AVAILABLE',
       s.base_price * (CASE st.seat_type WHEN 'REGULAR' THEN 1.0 WHEN 'PREMIUM' THEN 1.5 WHEN 'VIP' THEN 2.0 END),
       0
FROM shows s
JOIN seat st ON st.screen_id = s.screen_id;

-- Users
INSERT INTO platform_user (id, name, email, phone, role) VALUES
(1, 'Krishna Customer', 'krishna@example.com', '9999999999', 'CUSTOMER'),
(2, 'Theatre Admin PVR', 'admin.pvr@example.com', '8888888888', 'THEATRE_ADMIN');

-- Offers
-- Offer 1: 50% off on 3rd ticket (applies everywhere, no rules needed)
INSERT INTO offer (id, code, name, description, discount_type, discount_value, applies_to, nth_ticket_number, priority, stackable, active)
VALUES (1, 'THIRD_TICKET_50', '50% off on 3rd ticket', 'Get 50% off on your 3rd ticket in a single booking', 'PERCENTAGE', 50.00, 'NTH_TICKET', 3, 1, TRUE, TRUE);

-- Offer 2: 20% off on afternoon shows (12:00 - 17:00)
INSERT INTO offer (id, code, name, description, discount_type, discount_value, applies_to, priority, stackable, active)
VALUES (2, 'AFTERNOON_20', '20% off on afternoon shows', 'Tickets booked for afternoon shows (12 PM - 5 PM) get 20% off', 'PERCENTAGE', 20.00, 'PER_BOOKING', 2, TRUE, TRUE);

INSERT INTO offer_rule (offer_id, rule_type, rule_value) VALUES
(2, 'TIME_RANGE', '{"start":"12:00","end":"17:00"}');

-- Offer 3 (example): Flat ₹100 off at PVR Mumbai theatres
INSERT INTO offer (id, code, name, description, discount_type, discount_value, applies_to, priority, stackable, active)
VALUES (3, 'MUMBAI_PVR_100', 'Flat Rs.100 off at PVR Mumbai', 'Get flat Rs.100 off on bookings at PVR theatres in Mumbai', 'FLAT', 100.00, 'PER_BOOKING', 3, FALSE, TRUE);

INSERT INTO offer_city (offer_id, city_id) VALUES (3, 1);
INSERT INTO offer_theatre (offer_id, theatre_id) VALUES (3, 1);
