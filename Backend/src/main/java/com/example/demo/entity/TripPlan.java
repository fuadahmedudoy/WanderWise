package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "trip_plan")
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trip_plan", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String tripPlan;

    @Column(name = "status", nullable = false)
    @Convert(converter = TripStatusConverter.class)
    private TripStatus status = TripStatus.UPCOMING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TripStatus.UPCOMING;
        }
    }

    public enum TripStatus {
        UPCOMING("upcoming"),
        RUNNING("running"),
        COMPLETED("completed");

        private final String value;

        TripStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TripStatus fromValue(String value) {
            for (TripStatus status : TripStatus.values()) {
                if (status.getValue().equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status value: " + value);
        }
    }

    @Converter
    public static class TripStatusConverter implements AttributeConverter<TripStatus, String> {

        @Override
        public String convertToDatabaseColumn(TripStatus status) {
            return status != null ? status.getValue() : null;
        }

        @Override
        public TripStatus convertToEntityAttribute(String value) {
            return value != null ? TripStatus.fromValue(value) : null;
        }
    }
}
