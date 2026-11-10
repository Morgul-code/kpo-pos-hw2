package ru.kokoev.pos.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Criterion {
    private final UUID id;
    private final String name;
    private final int maxScore;
    private final BigDecimal weight;

    public Criterion(UUID id, String name, int maxScore, BigDecimal weight) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Error: Identifier (ID) cannot be null\n"
            );
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Error: Name cannot be empty or consist only of spaces.\n"
            );
        }
        if (maxScore <= 0) {
            throw new IllegalArgumentException(
                    "Error: maxScore cannot be less than or equal to zero.\n"
            );
        }
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0 || weight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Error: Grade weight must be greater than zero and less than or equal to one.\n"
            );
        }

        this.id = id;
        this.name = name;
        this.maxScore = maxScore;
        this.weight = weight;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public BigDecimal getWeight() {
        return weight;
    }
}
