package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Rubric;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RubricTest {
    private final Criterion firstCriterion;

    public RubricTest() {
        this.firstCriterion = new Criterion(
                UUID.randomUUID(),
                "Экзамен",
                10,
                new BigDecimal("0.70")
        );
    }

    @Test
    void shouldCreateValidCriterion() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.firstCriterion);
        assertEquals(1, rubric.getCriteria().size());
    }

    @Test
    void shouldRejectTotalWeightGreaterThanOne() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.firstCriterion);
        assertThrows(
                IllegalArgumentException.class,
                () -> rubric.addCriterion(
                        new Criterion(
                                UUID.randomUUID(),
                                "Проект",
                                10,
                                new BigDecimal("0.40")
                        )
                )
        );
    }
}
