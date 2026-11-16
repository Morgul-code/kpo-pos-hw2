package ru.kokoev.pos.application.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.kokoev.pos.application.port.ResultExporter;
import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.factory.EvaluationFactory;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.RubricRepository;
import ru.kokoev.pos.domain.repository.StudentRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.domain.strategy.WeightedScoringStrategy;
import ru.kokoev.pos.infrastructure.export.CsvResultExporter;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryRubricRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryStudentRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PosFacadeTest {
    private final StudentRepository studentRepository = new InMemoryStudentRepository();
    private final RubricRepository rubricRepository = new InMemoryRubricRepository();
    private final EvaluationApplicationService evaluationService = new EvaluationApplicationService(
            new InMemoryEvaluationRepository(),
            new FinalScoreService(new WeightedScoringStrategy()),
            new EvaluationFactory()
    );
    private final ResultExporter resultExporter = new CsvResultExporter();
    private final PosFacade facade = new PosFacade(
            this.studentRepository, this.rubricRepository, this.evaluationService, this.resultExporter
    );

    @Test
    void shouldCreateAndFindStudent() {
        Student student = this.facade.createStudent("Иван Иванов");

        assertEquals("Иван Иванов", student.getFullName());
        assertSame(student, this.studentRepository.getById(student.getId()));
        assertEquals(List.of(student), this.facade.findStudents());
    }

    @Test
    void shouldCreateAndFindRubric() {
        Rubric rubric = this.facade.createRubric();

        assertSame(rubric, this.rubricRepository.getById(rubric.getId()));
        assertEquals(List.of(rubric), this.facade.findRubrics());
    }

    @Test
    void shouldAddAndFindCriterion() {
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(
                rubric.getId(), "Экзамен", 10, BigDecimal.ONE
        );

        assertEquals("Экзамен", criterion.getName());
        assertEquals(10, criterion.getMaxScore());
        assertEquals(BigDecimal.ONE, criterion.getWeight());
        assertSame(criterion, this.rubricRepository.getById(rubric.getId())
                .getCriterionById(criterion.getId()));
        assertEquals(List.of(criterion), this.facade.findCriteria(rubric.getId()));
    }

    @Test
    void shouldAssignAndFindEvaluation() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);

        Evaluation evaluation = this.facade.assignScore(
                student.getId(), rubric.getId(), criterion.getId(), 8
        );

        assertEquals(8, evaluation.getScore().value());
        assertEquals(List.of(evaluation), this.facade.findEvaluations(student.getId(), rubric.getId()));
    }

    @Test
    void shouldCalculateFinalThroughApplicationService() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, new BigDecimal("0.60"));
        Criterion project = this.facade.addCriterion(rubric.getId(), "Проект", 20, new BigDecimal("0.40"));
        this.facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 8);
        this.facade.assignScore(student.getId(), rubric.getId(), project.getId(), 18);

        BigDecimal result = this.facade.calculateFinal(student.getId(), rubric.getId());

        assertEquals(0, new BigDecimal("0.84").compareTo(result));
    }

    @Test
    void shouldRejectDuplicateScoreWithoutChangingEvaluations() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);
        Evaluation original = this.facade.assignScore(
                student.getId(), rubric.getId(), criterion.getId(), 8
        );

        assertThrows(IllegalArgumentException.class, () -> this.facade.assignScore(
                student.getId(), rubric.getId(), criterion.getId(), 9
        ));
        assertEquals(List.of(original), this.facade.findEvaluations(student.getId(), rubric.getId()));
    }

    @Test
    void shouldRejectInvalidStudentWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> this.facade.createStudent(" "));
        assertTrue(this.facade.findStudents().isEmpty());
    }

    @Test
    void shouldRejectExcessiveWeightWithoutAddingCriterion() {
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> this.facade.addCriterion(
                rubric.getId(), "Проект", 20, new BigDecimal("0.40")
        ));
        assertEquals(List.of(criterion), this.facade.findCriteria(rubric.getId()));
    }

    @Test
    void shouldRejectAddingCriterionAfterEvaluation() {
        this.facade.createStudent("Пётр Петров");
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(
                rubric.getId(), "Экзамен", 10, new BigDecimal("0.60")
        );
        Criterion project = this.facade.addCriterion(
                rubric.getId(), "Проект", 20, new BigDecimal("0.40")
        );
        Evaluation evaluation = this.facade.assignScore(
                student.getId(), rubric.getId(), exam.getId(), 8
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> this.facade.addCriterion(
                        rubric.getId(), "Дополнительное задание", 5, new BigDecimal("0.10")
                )
        );

        assertEquals("Нельзя изменять рубрикатор после начала оценивания", exception.getMessage());
        assertEquals(List.of(exam, project), this.facade.findCriteria(rubric.getId()));
        assertEquals(0, BigDecimal.ONE.compareTo(rubric.getTotalWeight()));
        assertEquals(List.of(evaluation), this.facade.findEvaluations(student.getId(), rubric.getId()));
    }

    @Test
    void shouldAllowAddingCriterionToOtherRubricAfterEvaluation() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric gradedRubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(
                gradedRubric.getId(), "Экзамен", 10, BigDecimal.ONE
        );
        this.facade.assignScore(student.getId(), gradedRubric.getId(), exam.getId(), 8);
        Rubric otherRubric = this.facade.createRubric();

        Criterion project = this.facade.addCriterion(
                otherRubric.getId(), "Проект", 20, BigDecimal.ONE
        );

        assertEquals(List.of(project), this.facade.findCriteria(otherRubric.getId()));
    }

    @Test
    void shouldAllowCompletingRubricAfterRejectedGrading() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(
                rubric.getId(), "Экзамен", 10, new BigDecimal("0.60")
        );
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> this.facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 8)
        );
        assertEquals(
                "Нельзя выставлять оценки: сумма весов рубрикатора должна равняться 1",
                exception.getMessage()
        );
        assertTrue(this.facade.findEvaluations(student.getId(), rubric.getId()).isEmpty());

        Criterion project = this.facade.addCriterion(
                rubric.getId(), "Проект", 20, new BigDecimal("0.40")
        );

        assertEquals(List.of(exam, project), this.facade.findCriteria(rubric.getId()));
        assertEquals(0, BigDecimal.ONE.compareTo(rubric.getTotalWeight()));
        Evaluation evaluation = this.facade.assignScore(
                student.getId(), rubric.getId(), exam.getId(), 8
        );
        assertEquals(List.of(evaluation), this.facade.findEvaluations(student.getId(), rubric.getId()));
        assertEquals(0, new BigDecimal("0.48").compareTo(
                this.facade.calculateFinal(student.getId(), rubric.getId())
        ));
    }

    @Test
    void shouldRejectUnknownStudent() {
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);
        UUID unknownId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> this.facade.assignScore(
                unknownId, rubric.getId(), criterion.getId(), 8
        ));
        assertThrows(IllegalArgumentException.class,
                () -> this.facade.findEvaluations(unknownId, rubric.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> this.facade.calculateFinal(unknownId, rubric.getId()));
    }

    @Test
    void shouldRejectUnknownRubric() {
        Student student = this.facade.createStudent("Иван Иванов");
        UUID unknownId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> this.facade.addCriterion(unknownId, "Экзамен", 10, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> this.facade.findCriteria(unknownId));
        assertThrows(IllegalArgumentException.class, () -> this.facade.assignScore(
                student.getId(), unknownId, UUID.randomUUID(), 8
        ));
        assertThrows(IllegalArgumentException.class,
                () -> this.facade.findEvaluations(student.getId(), unknownId));
        assertThrows(IllegalArgumentException.class,
                () -> this.facade.calculateFinal(student.getId(), unknownId));
    }

    @Test
    void shouldUpdateAndRemoveScoreBeforeCsvExport(@TempDir Path tempDir) throws IOException {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, new BigDecimal("0.60"));
        Criterion project = this.facade.addCriterion(rubric.getId(), "Проект", 20, new BigDecimal("0.40"));
        Evaluation original = this.facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 3);
        this.facade.assignScore(student.getId(), rubric.getId(), project.getId(), 18);

        Evaluation updated = this.facade.updateScore(student.getId(), rubric.getId(), exam.getId(), 8);

        assertEquals(original.getId(), updated.getId());
        assertEquals(0, new BigDecimal("0.84").compareTo(
                this.facade.calculateFinal(student.getId(), rubric.getId())
        ));
        assertThrows(IllegalArgumentException.class,
                () -> this.facade.updateScore(student.getId(), rubric.getId(), exam.getId(), 12));
        assertEquals(0, new BigDecimal("0.84").compareTo(
                this.facade.calculateFinal(student.getId(), rubric.getId())
        ));

        this.facade.removeScore(student.getId(), rubric.getId(), exam.getId());
        Path destination = tempDir.resolve("results.csv");
        this.facade.exportResult(student.getId(), rubric.getId(), destination);

        assertEquals(
                "student_id,student_name,rubric_id,criterion_name,score,max_score,weight,final_percent\n"
                        + student.getId() + ",\"Иван Иванов\"," + rubric.getId() + ",\"Экзамен\",,10,0.60,36\n"
                        + student.getId() + ",\"Иван Иванов\"," + rubric.getId() + ",\"Проект\",18,20,0.40,36\n",
                Files.readString(destination)
        );
    }

    @Test
    void shouldKeepGradedRubricWhenReplacementIsRejected() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion exam = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);
        this.facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 8);
        Rubric replacement = new Rubric(rubric.getId());
        replacement.addCriterion(new Criterion(UUID.randomUUID(), "Другой критерий", 20, BigDecimal.ONE));

        assertThrows(IllegalArgumentException.class, () -> this.rubricRepository.save(replacement));

        assertEquals(List.of(exam), this.facade.findCriteria(rubric.getId()));
        assertEquals(0, new BigDecimal("0.8").compareTo(
                this.facade.calculateFinal(student.getId(), rubric.getId())
        ));
    }

    @Test
    void shouldNotCreateFileForInvalidRubric(@TempDir Path tempDir) {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        this.facade.addCriterion(rubric.getId(), "Экзамен", 10, new BigDecimal("0.60"));
        Path destination = tempDir.resolve("results.csv");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> this.facade.exportResult(student.getId(), rubric.getId(), destination)
        );

        assertEquals("Сумма весов рубрикатора должна равняться единице", exception.getMessage());
        assertFalse(Files.exists(destination));
    }

    @Test
    void shouldNotOverwriteFileForInvalidRubric(@TempDir Path tempDir) throws IOException {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Path destination = tempDir.resolve("results.csv");
        Files.writeString(destination, "Существующий результат");

        assertThrows(
                IllegalArgumentException.class,
                () -> this.facade.exportResult(student.getId(), rubric.getId(), destination)
        );

        assertEquals("Существующий результат", Files.readString(destination));
    }

    @Test
    void shouldRejectNullExportDestination() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();

        assertThrows(
                IllegalArgumentException.class,
                () -> this.facade.exportResult(student.getId(), rubric.getId(), null)
        );
    }

    @Test
    void shouldRejectNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new PosFacade(null, this.rubricRepository, this.evaluationService, this.resultExporter));
        assertThrows(IllegalArgumentException.class,
                () -> new PosFacade(this.studentRepository, null, this.evaluationService, this.resultExporter));
        assertThrows(IllegalArgumentException.class,
                () -> new PosFacade(this.studentRepository, this.rubricRepository, null, this.resultExporter));
        assertThrows(IllegalArgumentException.class,
                () -> new PosFacade(this.studentRepository, this.rubricRepository, this.evaluationService, null));
    }
}
