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
        assertEquals(1, rubric.getCriteria().size());
        assertEquals(
                0,
                new BigDecimal("0.70").compareTo(rubric.getTotalWeight())
        );
    }

    @Test
    void shouldRejectDuplicateCriterionId() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.firstCriterion);
        Criterion duplicateCriterion = new Criterion(
                this.firstCriterion.getId(),
                "Проект",
                10,
                new BigDecimal("0.20")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rubric.addCriterion(duplicateCriterion)
        );
        assertEquals(1, rubric.getCriteria().size());
        assertEquals(this.firstCriterion, rubric.getCriteria().get(0));
        assertEquals(
                0,
                new BigDecimal("0.70").compareTo(rubric.getTotalWeight())
        );
    }

    @Test
    void shouldRejectNullCriterion() {
        Rubric rubric = new Rubric(UUID.randomUUID());

        assertThrows(
                IllegalArgumentException.class,
                () -> rubric.addCriterion(null)
        );
        assertTrue(rubric.getCriteria().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(rubric.getTotalWeight()));
    }

    @Test
    void shouldAcceptTotalWeightEqualToOne() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.firstCriterion);
        Criterion secondCriterion = new Criterion(
                UUID.randomUUID(),
                "Проект",
                10,
                new BigDecimal("0.30")
        );

        assertDoesNotThrow(() -> rubric.addCriterion(secondCriterion));
        assertEquals(2, rubric.getCriteria().size());
        assertEquals(
                0,
                new BigDecimal("1.00").compareTo(rubric.getTotalWeight())
        );
    }

    @Test
    void shouldReturnUnmodifiableCriteria() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.firstCriterion);

        assertThrows(
                UnsupportedOperationException.class,
                () -> rubric.getCriteria().add(this.firstCriterion)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> rubric.getCriteria().remove(this.firstCriterion)
        );
        assertEquals(1, rubric.getCriteria().size());
        assertEquals(this.firstCriterion, rubric.getCriteria().get(0));
    }
}
