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
}
