package com.bookingplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "offer_city")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}
