package com.bookingplatform.entity;

import com.bookingplatform.enums.TheatreStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theatre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theatre extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "total_screens", nullable = false)
    private Integer totalScreens;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TheatreStatus status;
}
