package com.bers.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "configs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Config {

    // Constantes para configuraciones comunes
    public static final String SEAT_HOLD_MINUTES = "seat.hold.minutes";
    public static final String NO_SHOW_FEE = "no.show.fee";
    public static final String BAGGAGE_FREE_WEIGHT_KG = "baggage.free.weight.kg";
    public static final String BAGGAGE_EXTRA_PRICE_PER_KG = "baggage.extra.price.per.kg";
    public static final String CANCELLATION_FULL_REFUND_HOURS = "cancellation.full.refund.hours";
    public static final String CANCELLATION_PARTIAL_REFUND_HOURS = "cancellation.partial.refund.hours";
    public static final String CANCELLATION_PARTIAL_REFUND_PERCENTAGE = "cancellation.partial.refund.percentage";
    public static final String OVERBOOKING_PERCENTAGE = "overbooking.percentage";
    public static final String OVERBOOKING_MAX_PERCENTAGE = "overbooking.max.percentage";
    public static final String OVERBOOKING_REQUIRES_APPROVAL = "overbooking.requires_approval";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100, name = "config_key")
    private String key;
    @Column(nullable = false, columnDefinition = "TEXT", name = "config_value")
    private String value;
    @Column(length = 255)
    private String description;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

