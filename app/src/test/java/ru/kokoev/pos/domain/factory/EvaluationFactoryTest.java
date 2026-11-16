package ru.kokoev.pos.domain.factory;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationFactoryTest {
    private final EvaluationFactory factory = new EvaluationFactory();
    private final Student student = new Student(UUID.randomUUID(), "Иван Иванов");
    private final Rubric rubric = new Rubric(UUID.randomUUID());
    private final Criterion criterion = new Criterion(
            UUID.randomUUID(), "Экзамен", 10, new BigDecimal("1.00")
    );

    public EvaluationFactoryTest() {
        this.rubric.addCriterion(this.criterion);
    }

    @Test
    void shouldCreateValidEvaluation() {
        Evaluation evaluation = this.factory.create(
                this.student, this.rubric, this.criterion.getId(), 8
        );

        assertNotNull(evaluation.getId());
        assertEquals(this.student.getId(), evaluation.getStudentId());
        assertEquals(this.rubric.getId(), evaluation.getRubricId());
        assertEquals(this.criterion.getId(), evaluation.getCriterionId());
        assertEquals(new Score(8), evaluation.getScore());
    }

    @Test
    void shouldGenerateDifferentIds() {
        Evaluation first = this.factory.create(
                this.student, this.rubric, this.criterion.getId(), 8
        );
        Evaluation second = this.factory.create(
                this.student, this.rubric, this.criterion.getId(), 8
        );

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void shouldPreserveProvidedIdForCorrection() {
        UUID id = UUID.randomUUID();

        Evaluation evaluation = this.factory.create(
                id, this.student, this.rubric, this.criterion.getId(), 8
        );

        assertEquals(id, evaluation.getId());
        assertEquals(new Score(8), evaluation.getScore());
        assertThrows(IllegalArgumentException.class,
                () -> this.factory.create(null, this.student, this.rubric, this.criterion.getId(), 8));
    }

    @Test
    void shouldRejectNegativeScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, this.rubric, this.criterion.getId(), -1)
        );
    }

    @Test
    void shouldRejectScoreAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, this.rubric, this.criterion.getId(), 12)
        );
    }

    @Test
    void shouldRejectUnknownCriterion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, this.rubric, UUID.randomUUID(), 8)
        );
    }

    @Test
    void shouldRejectIncompleteRubric() {
        Rubric incompleteRubric = new Rubric(UUID.randomUUID());
        Criterion exam = new Criterion(
                UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60")
        );
        incompleteRubric.addCriterion(exam);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, incompleteRubric, exam.getId(), 8)
        );
        assertEquals(
                "Нельзя выставлять оценки: сумма весов рубрикатора должна равняться 1",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(null, this.rubric, this.criterion.getId(), 8)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, null, this.criterion.getId(), 8)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.factory.create(this.student, this.rubric, null, 8)
        );
    }
}
