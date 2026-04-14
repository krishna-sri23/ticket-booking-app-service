package com.bookingplatform.entity;

import com.bookingplatform.enums.AppliesTo;
import com.bookingplatform.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false)
    private AppliesTo appliesTo;

    @Column(name = "nth_ticket_number")
    private Integer nthTicketNumber;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean stackable;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OfferRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OfferCity> cities = new ArrayList<>();

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OfferTheatre> theatres = new ArrayList<>();
}
