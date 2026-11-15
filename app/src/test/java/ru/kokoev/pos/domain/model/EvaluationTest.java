package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTest {
    private final Student student;
    private final Rubric rubric;
    private final Criterion criterion;

    public EvaluationTest() {
        this.student = new Student(UUID.randomUUID(), "Иван Иванов");
        this.rubric = new Rubric(UUID.randomUUID());
        this.criterion = new Criterion(
                UUID.randomUUID(),
                "Экзамен",
                10,
                BigDecimal.ONE
        );
        this.rubric.addCriterion(this.criterion);
    }

    @Test
    void shouldAcceptValidScore() {
        UUID id = UUID.randomUUID();
        Score score = new Score(8);

        Evaluation evaluation = new Evaluation(
                id, this.student, this.rubric, this.criterion.getId(), score
        );

        assertEquals(id, evaluation.getId());
        assertEquals(this.student.getId(), evaluation.getStudentId());
        assertEquals(this.rubric.getId(), evaluation.getRubricId());
        assertEquals(this.criterion.getId(), evaluation.getCriterionId());
        assertEquals(score, evaluation.getScore());
    }

    @Test
    void shouldRejectScoreGreaterThanMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Evaluation(
                        UUID.randomUUID(), this.student, this.rubric,
                        this.criterion.getId(), new Score(12)
                )
        );
    }

    @Test
    void shouldRejectMissingCriterion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Evaluation(
                        UUID.randomUUID(), this.student, this.rubric,
                        UUID.randomUUID(), new Score(8)
                )
        );
    }

    @Test
    void shouldRejectNullScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Evaluation(
                        UUID.randomUUID(), this.student, this.rubric,
                        this.criterion.getId(), null
                )
        );
    }

    @Test
    void shouldAcceptScoreEqualToMaximum() {
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, this.rubric,
                this.criterion.getId(), new Score(10)
        );

        assertEquals(10, evaluation.getScore().value());
    }
}
