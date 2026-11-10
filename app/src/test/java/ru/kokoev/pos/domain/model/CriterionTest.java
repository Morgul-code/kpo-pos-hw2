package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;


public class CriterionTest {
    @Test
    void shouldCreateCriterionWithValidWeight() {
        BigDecimal weight = new BigDecimal("0.40");

        Criterion criterion = new Criterion(
                UUID.randomUUID(),
                "Экзамен",
                10,
                weight
        );

        assertEquals(
                0,
                weight.compareTo(criterion.getWeight())
        );
    }

    @Test
    void shouldCreateCriterionWithValidName() {
        BigDecimal weight = new BigDecimal("0.40");

        Criterion criterion = new Criterion(
                UUID.randomUUID(),
                "Экзамен",
                10,
                weight
        );

        assertEquals(
                "Экзамен",
                criterion.getName()
        );
    }

    @Test
    void shouldCreateCriterionWithValidMaxScore() {
        BigDecimal weight = new BigDecimal("0.60");

        Criterion criterion = new Criterion(
                UUID.randomUUID(),
                "Экзамен",
                5,
                weight
        );

        assertEquals(
                "Экзамен",
                criterion.getName()
        );
    }

    @Test
    void shouldRejectMaxScoreEqualZero() {
        BigDecimal weight = new BigDecimal("0.40");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Criterion(
                        UUID.randomUUID(),
                        "Экзамен",
                        0,
                        weight
                )
        );
    }

    @Test
    void shouldRejectWeightEqualNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Criterion(
                        UUID.randomUUID(),
                        "Экзамен",
                        10,
                        null
                )
        );
    }

    @Test
    void shouldRejectWeightBiggerThanZero() {
        BigDecimal weight = new BigDecimal("1.10");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Criterion(
                        UUID.randomUUID(),
                        "Экзамен",
                        10,
                        weight
                )
        );
    }

    @Test
    void shouldRejectNameIsBlank() {
        BigDecimal weight = new BigDecimal("0.40");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Criterion(
                        UUID.randomUUID(),
                        "",
                        0,
                        weight
                )
        );
    }
}
