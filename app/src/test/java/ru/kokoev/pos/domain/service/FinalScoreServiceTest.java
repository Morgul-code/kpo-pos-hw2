package ru.kokoev.pos.domain.service;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FinalScoreServiceTest {
    private final FinalScoreService service;
    private final Student student;
    private final Rubric rubric;
    private final Criterion exam;
    private final Criterion project;

    public FinalScoreServiceTest() {
        this.service = new FinalScoreService();
        this.student = new Student(UUID.randomUUID(), "Иван Иванов");
        this.rubric = new Rubric(UUID.randomUUID());
        this.exam = new Criterion(UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60"));
        this.project = new Criterion(UUID.randomUUID(), "Проект", 20, new BigDecimal("0.40"));
        this.rubric.addCriterion(this.exam);
        this.rubric.addCriterion(this.project);
    }

    @Test
    void shouldCalculateWeightedScore() {
        BigDecimal result = this.service.calculate(
                this.student.getId(), this.rubric,
                List.of(evaluation(this.exam, 8), evaluation(this.project, 18))
        );

        assertEquals(0, new BigDecimal("0.84").compareTo(result));
    }

    @Test
    void shouldTreatMissingEvaluationAsZero() {
        BigDecimal result = this.service.calculate(
                this.student.getId(), this.rubric,
                List.of(evaluation(this.exam, 8))
        );

        assertEquals(0, new BigDecimal("0.48").compareTo(result));
    }

    @Test
    void shouldReturnZeroForEmptyEvaluations() {
        BigDecimal result = this.service.calculate(
                this.student.getId(), this.rubric, List.of()
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void shouldReturnZeroForZeroScores() {
        BigDecimal result = this.service.calculate(
                this.student.getId(), this.rubric,
                List.of(evaluation(this.exam, 0), evaluation(this.project, 0))
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void shouldReturnOneForMaximumScores() {
        BigDecimal result = this.service.calculate(
                this.student.getId(), this.rubric,
                List.of(evaluation(this.exam, 10), evaluation(this.project, 20))
        );

        assertEquals(0, BigDecimal.ONE.compareTo(result));
    }

    @Test
    void shouldRoundNonTerminatingDivision() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        Criterion criterion = new Criterion(UUID.randomUUID(), "Задание", 3, BigDecimal.ONE);
        rubric.addCriterion(criterion);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, rubric, criterion.getId(), new Score(2)
        );

        BigDecimal result = this.service.calculate(
                this.student.getId(), rubric, List.of(evaluation)
        );

        assertEquals(0, new BigDecimal("0.6666666667").compareTo(result));
    }

    @Test
    void shouldRejectIncompleteRubricWeights() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        rubric.addCriterion(this.exam);

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(this.student.getId(), rubric, List.of())
        );
    }

    @Test
    void shouldRejectEmptyRubric() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), new Rubric(UUID.randomUUID()), List.of()
                )
        );
    }

    @Test
    void shouldRejectDuplicateCriterionEvaluations() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric,
                        List.of(evaluation(this.exam, 8), evaluation(this.exam, 9))
                )
        );
    }

    @Test
    void shouldRejectEvaluationForOtherStudent() {
        Student otherStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), otherStudent, this.rubric, this.exam.getId(), new Score(8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric, List.of(evaluation)
                )
        );
    }

    @Test
    void shouldRejectEvaluationForOtherRubric() {
        Rubric otherRubric = new Rubric(UUID.randomUUID());
        otherRubric.addCriterion(this.exam);
        otherRubric.addCriterion(this.project);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, otherRubric, this.exam.getId(), new Score(8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric, List.of(evaluation)
                )
        );
    }

    @Test
    void shouldRejectUnknownCriterion() {
        Rubric otherRubric = new Rubric(this.rubric.getId());
        Criterion unknownCriterion = new Criterion(
                UUID.randomUUID(), "Задание", 10, BigDecimal.ONE
        );
        otherRubric.addCriterion(unknownCriterion);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, otherRubric, unknownCriterion.getId(), new Score(8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric, List.of(evaluation)
                )
        );
    }

    @Test
    void shouldValidateScoreAgainstProvidedRubric() {
        Rubric otherRubric = new Rubric(this.rubric.getId());
        Criterion otherExam = new Criterion(this.exam.getId(), "Экзамен", 20, BigDecimal.ONE);
        otherRubric.addCriterion(otherExam);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, otherRubric, otherExam.getId(), new Score(12)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric, List.of(evaluation)
                )
        );
    }

    @Test
    void shouldRejectNullArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(null, this.rubric, List.of())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(this.student.getId(), null, List.of())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(this.student.getId(), this.rubric, null)
        );
    }

    @Test
    void shouldRejectNullEvaluation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculate(
                        this.student.getId(), this.rubric, Collections.singletonList(null)
                )
        );
    }

    private Evaluation evaluation(Criterion criterion, int score) {
        return new Evaluation(
                UUID.randomUUID(), this.student, this.rubric,
                criterion.getId(), new Score(score)
        );
    }
}
