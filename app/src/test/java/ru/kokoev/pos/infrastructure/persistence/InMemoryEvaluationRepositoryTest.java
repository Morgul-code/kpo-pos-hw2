package ru.kokoev.pos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.EvaluationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryEvaluationRepositoryTest {
    private final EvaluationRepository repository;
    private final Student student;
    private final Rubric rubric;
    private final Criterion criterion;

    public InMemoryEvaluationRepositoryTest() {
        this.repository = new InMemoryEvaluationRepository();
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
    void shouldFindSavedEvaluationByStudentAndRubric() {
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, this.rubric,
                this.criterion.getId(), new Score(8)
        );
        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ).isEmpty());

        this.repository.save(evaluation);

        assertEquals(
                List.of(evaluation),
                this.repository.findByStudentAndRubric(
                        this.student.getId(), this.rubric.getId()
                )
        );
    }

    @Test
    void shouldExcludeEvaluationsForOtherStudentOrRubric() {
        Student otherStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        Rubric otherRubric = new Rubric(UUID.randomUUID());
        otherRubric.addCriterion(this.criterion);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, this.rubric,
                this.criterion.getId(), new Score(8)
        );
        this.repository.save(new Evaluation(
                UUID.randomUUID(), otherStudent, this.rubric,
                this.criterion.getId(), new Score(7)
        ));
        this.repository.save(new Evaluation(
                UUID.randomUUID(), this.student, otherRubric,
                this.criterion.getId(), new Score(9)
        ));
        this.repository.save(evaluation);

        assertEquals(
                List.of(evaluation),
                this.repository.findByStudentAndRubric(
                        this.student.getId(), this.rubric.getId()
                )
        );
    }

    @Test
    void shouldReturnEmptyListInitially() {
        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ).isEmpty());
    }

    @Test
    void shouldRejectNullEvaluation() {
        assertThrows(IllegalArgumentException.class, () -> this.repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> this.repository.update(null));
        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ).isEmpty());
    }

    @Test
    void shouldRejectDuplicateCompositeKeyWithoutReplacingOriginal() {
        Evaluation original = evaluation(UUID.randomUUID(), 3);
        this.repository.save(original);
        Evaluation duplicate = evaluation(UUID.randomUUID(), 8);

        assertThrows(IllegalArgumentException.class, () -> this.repository.save(duplicate));
        assertThrows(IllegalArgumentException.class, () -> this.repository.save(original));

        assertEquals(List.of(original), this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ));
    }

    @Test
    void shouldUpdateExistingEvaluationAndPreserveSnapshots() {
        Evaluation original = evaluation(UUID.randomUUID(), 3);
        this.repository.save(original);
        List<Evaluation> snapshot = this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        );
        Evaluation updated = evaluation(original.getId(), 8);

        this.repository.update(updated);

        assertEquals(List.of(updated), this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ));
        assertEquals(List.of(original), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(updated));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(original));
    }

    @Test
    void shouldRejectUpdateOfMissingEvaluation() {
        Evaluation evaluation = evaluation(UUID.randomUUID(), 8);

        assertThrows(IllegalArgumentException.class, () -> this.repository.update(evaluation));
        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ).isEmpty());
    }

    @Test
    void shouldRejectChangingEvaluationId() {
        Evaluation original = evaluation(UUID.randomUUID(), 3);
        this.repository.save(original);
        Evaluation replacement = evaluation(UUID.randomUUID(), 8);

        assertThrows(IllegalArgumentException.class, () -> this.repository.update(replacement));
        assertEquals(List.of(original), this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ));
    }

    @Test
    void shouldRemoveOnlyMatchingEvaluation() {
        Evaluation original = evaluation(UUID.randomUUID(), 3);
        Student otherStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        Rubric otherRubric = new Rubric(UUID.randomUUID());
        otherRubric.addCriterion(this.criterion);
        Evaluation otherStudentEvaluation = new Evaluation(
                UUID.randomUUID(), otherStudent, this.rubric, this.criterion.getId(), new Score(8)
        );
        Evaluation otherRubricEvaluation = new Evaluation(
                UUID.randomUUID(), this.student, otherRubric, this.criterion.getId(), new Score(9)
        );
        this.repository.save(original);
        this.repository.save(otherStudentEvaluation);
        this.repository.save(otherRubricEvaluation);

        this.repository.remove(this.student.getId(), this.rubric.getId(), this.criterion.getId());

        assertTrue(this.repository.findByStudentAndRubric(
                this.student.getId(), this.rubric.getId()
        ).isEmpty());
        assertEquals(List.of(otherStudentEvaluation), this.repository.findByStudentAndRubric(
                otherStudent.getId(), this.rubric.getId()
        ));
        assertEquals(List.of(otherRubricEvaluation), this.repository.findByStudentAndRubric(
                this.student.getId(), otherRubric.getId()
        ));
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.remove(this.student.getId(), this.rubric.getId(), this.criterion.getId()));
    }

    @Test
    void shouldRejectNullSearchAndRemovalIds() {
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.findByStudentAndRubric(null, this.rubric.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.findByStudentAndRubric(this.student.getId(), null));
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.remove(null, this.rubric.getId(), this.criterion.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.remove(this.student.getId(), null, this.criterion.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.repository.remove(this.student.getId(), this.rubric.getId(), null));
    }

    private Evaluation evaluation(UUID id, int points) {
        return new Evaluation(id, this.student, this.rubric, this.criterion.getId(), new Score(points));
    }
}
