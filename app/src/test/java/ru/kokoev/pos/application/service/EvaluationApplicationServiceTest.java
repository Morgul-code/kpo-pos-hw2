package ru.kokoev.pos.application.service;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.factory.EvaluationFactory;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.domain.strategy.WeightedScoringStrategy;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationApplicationServiceTest {
    private final EvaluationRepository repository;
    private final FinalScoreService finalScoreService;
    private final EvaluationFactory evaluationFactory;
    private final EvaluationApplicationService service;
    private final Student student;
    private final Rubric rubric;
    private final Criterion exam;
    private final Criterion project;

    public EvaluationApplicationServiceTest() {
        this.repository = new InMemoryEvaluationRepository();
        this.finalScoreService = new FinalScoreService(new WeightedScoringStrategy());
        this.evaluationFactory = new EvaluationFactory();
        this.service = new EvaluationApplicationService(
                this.repository, this.finalScoreService, this.evaluationFactory
        );
        this.student = new Student(UUID.randomUUID(), "Иван Иванов");
        this.rubric = new Rubric(UUID.randomUUID());
        this.exam = new Criterion(UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60"));
        this.project = new Criterion(UUID.randomUUID(), "Проект", 20, new BigDecimal("0.40"));
        this.rubric.addCriterion(this.exam);
        this.rubric.addCriterion(this.project);
    }

    @Test
    void shouldAssignAndSaveScore() {
        Evaluation evaluation = this.service.assignScore(
                this.student, this.rubric, this.exam.getId(), 8
        );

        assertNotNull(evaluation.getId());
        assertEquals(this.student.getId(), evaluation.getStudentId());
        assertEquals(this.rubric.getId(), evaluation.getRubricId());
        assertEquals(this.exam.getId(), evaluation.getCriterionId());
        assertEquals(new Score(8), evaluation.getScore());
        assertEquals(
                List.of(evaluation),
                this.repository.findByStudentAndRubric(this.student.getId(), this.rubric.getId())
        );
        assertEquals(List.of(evaluation), this.service.findEvaluations(this.student, this.rubric));
    }

    @Test
    void shouldRejectDuplicateCriterionScoreWithoutChangingSavedEvaluations() {
        Evaluation original = this.service.assignScore(
                this.student, this.rubric, this.exam.getId(), 8
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, this.rubric, this.exam.getId(), 9)
        );
        assertEquals(List.of(original), this.service.findEvaluations(this.student, this.rubric));
    }

    @Test
    void shouldRejectScoreAboveMaximumWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, this.rubric, this.exam.getId(), 12)
        );

        assertTrue(this.service.findEvaluations(this.student, this.rubric).isEmpty());
    }

    @Test
    void shouldCalculateFinalScore() {
        this.service.assignScore(this.student, this.rubric, this.exam.getId(), 8);
        this.service.assignScore(this.student, this.rubric, this.project.getId(), 18);

        BigDecimal result = this.service.calculateFinal(this.student, this.rubric);

        assertEquals(0, new BigDecimal("0.84").compareTo(result));
    }

    @Test
    void shouldUpdateScoreAndFinalResultWithoutChangingEvaluationId() {
        Evaluation original = this.service.assignScore(this.student, this.rubric, this.exam.getId(), 3);
        assertEquals(0, new BigDecimal("0.18").compareTo(
                this.service.calculateFinal(this.student, this.rubric)
        ));

        Evaluation updated = this.service.updateScore(this.student, this.rubric, this.exam.getId(), 8);

        assertEquals(original.getId(), updated.getId());
        assertEquals(new Score(8), updated.getScore());
        assertEquals(List.of(updated), this.service.findEvaluations(this.student, this.rubric));
        assertEquals(0, new BigDecimal("0.48").compareTo(
                this.service.calculateFinal(this.student, this.rubric)
        ));
    }

    @Test
    void shouldKeepOriginalAfterInvalidCorrection() {
        Evaluation original = this.service.assignScore(this.student, this.rubric, this.exam.getId(), 3);

        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, this.rubric, this.exam.getId(), 12));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, this.rubric, this.exam.getId(), -1));

        assertEquals(List.of(original), this.service.findEvaluations(this.student, this.rubric));
        assertEquals(0, new BigDecimal("0.18").compareTo(
                this.service.calculateFinal(this.student, this.rubric)
        ));
    }

    @Test
    void shouldRemoveScoreFromFinalCalculationAndAllowReassignment() {
        this.service.assignScore(this.student, this.rubric, this.exam.getId(), 8);
        Evaluation projectEvaluation = this.service.assignScore(
                this.student, this.rubric, this.project.getId(), 18
        );

        this.service.removeScore(this.student, this.rubric, this.exam.getId());

        assertEquals(List.of(projectEvaluation), this.service.findEvaluations(this.student, this.rubric));
        assertEquals(0, new BigDecimal("0.36").compareTo(
                this.service.calculateFinal(this.student, this.rubric)
        ));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> this.service.removeScore(this.student, this.rubric, this.exam.getId()));
        assertEquals("Оценка по данному критерию не найдена", exception.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, this.rubric, this.exam.getId(), 8));
        assertEquals(List.of(projectEvaluation), this.service.findEvaluations(this.student, this.rubric));

        this.service.assignScore(this.student, this.rubric, this.exam.getId(), 8);
        assertEquals(0, new BigDecimal("0.84").compareTo(
                this.service.calculateFinal(this.student, this.rubric)
        ));
    }

    @Test
    void shouldRejectChangingMissingScore() {
        IllegalArgumentException updateException = assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, this.rubric, this.exam.getId(), 8));
        IllegalArgumentException removeException = assertThrows(IllegalArgumentException.class,
                () -> this.service.removeScore(this.student, this.rubric, this.exam.getId()));

        assertEquals("Оценка по данному критерию не найдена", updateException.getMessage());
        assertEquals(updateException.getMessage(), removeException.getMessage());
        assertTrue(this.service.findEvaluations(this.student, this.rubric).isEmpty());
    }

    @Test
    void shouldRejectNullCorrectionAndRemovalArguments() {
        Evaluation original = this.service.assignScore(this.student, this.rubric, this.exam.getId(), 3);

        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(null, this.rubric, this.exam.getId(), 8));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, null, this.exam.getId(), 8));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.updateScore(this.student, this.rubric, null, 8));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.removeScore(null, this.rubric, this.exam.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.removeScore(this.student, null, this.exam.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.service.removeScore(this.student, this.rubric, null));

        assertEquals(List.of(original), this.service.findEvaluations(this.student, this.rubric));
    }

    @Test
    void shouldRejectNullDependencies() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvaluationApplicationService(null, this.finalScoreService, this.evaluationFactory)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvaluationApplicationService(this.repository, null, this.evaluationFactory)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvaluationApplicationService(this.repository, this.finalScoreService, null)
        );
    }

    @Test
    void shouldRejectNullAssignmentArgumentsWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(null, this.rubric, this.exam.getId(), 8)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, null, this.exam.getId(), 8)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, this.rubric, null, 8)
        );
        assertTrue(this.service.findEvaluations(this.student, this.rubric).isEmpty());
    }

    @Test
    void shouldRejectNullSearchArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.findEvaluations(null, this.rubric)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.findEvaluations(this.student, null)
        );
    }

    @Test
    void shouldRejectNullCalculationArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculateFinal(null, this.rubric)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.calculateFinal(this.student, null)
        );
    }

    @Test
    void shouldRejectNegativeScoreWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, this.rubric, this.exam.getId(), -1)
        );

        assertTrue(this.service.findEvaluations(this.student, this.rubric).isEmpty());
    }

    @Test
    void shouldRejectIncompleteRubricWithoutSaving() {
        Rubric incompleteRubric = new Rubric(UUID.randomUUID());
        incompleteRubric.addCriterion(this.exam);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, incompleteRubric, this.exam.getId(), 8)
        );

        assertEquals(
                "Нельзя выставлять оценки: сумма весов рубрикатора должна равняться 1",
                exception.getMessage()
        );
        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), incompleteRubric.getId()
        ).isEmpty());
    }

    @Test
    void shouldRejectUnknownCriterionWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.service.assignScore(this.student, this.rubric, UUID.randomUUID(), 8)
        );

        assertTrue(this.service.findEvaluations(this.student, this.rubric).isEmpty());
    }
}
