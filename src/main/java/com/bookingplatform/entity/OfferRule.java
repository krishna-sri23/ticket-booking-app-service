package com.bookingplatform.entity;

import com.bookingplatform.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "offer_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "rule_value", nullable = false, length = 1000)
    private String ruleValue;
}
