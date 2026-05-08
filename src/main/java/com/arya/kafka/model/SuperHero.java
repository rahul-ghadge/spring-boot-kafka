package com.arya.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Domain model representing a SuperHero entity published to and consumed from Kafka topics.
 *
 * <p>Implements {@link Serializable} to support safe serialization across Kafka partitions.
 * Jackson annotations ensure forward compatibility with unknown properties.
 *
 * @author rahul-ghadge
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperHero implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String superName;
    private String profession;
    private int age;
    private boolean canFly;

    /** Timestamp set by the producer at publish time. */
    @Builder.Default
    private Instant publishedAt = Instant.now();
}
