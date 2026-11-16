package ru.kokoev.pos.infrastructure.di;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.kokoev.pos.application.facade.PosFacade;
import ru.kokoev.pos.application.port.ResultExporter;
import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.repository.RubricRepository;
import ru.kokoev.pos.domain.repository.StudentRepository;
import ru.kokoev.pos.presentation.console.ConsoleMenu;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PosModuleTest {
    private final Injector injector = Guice.createInjector(new PosModule());

    @Test
    void shouldCreatePosFacade() {
        PosFacade facade = this.injector.getInstance(PosFacade.class);

        assertNotNull(facade);
        assertSame(facade, this.injector.getInstance(PosFacade.class));
    }

    @Test
    void shouldUseSingletonRepositories() {
        assertSame(
                this.injector.getInstance(StudentRepository.class),
                this.injector.getInstance(StudentRepository.class)
        );
        assertSame(
                this.injector.getInstance(RubricRepository.class),
                this.injector.getInstance(RubricRepository.class)
        );
        assertSame(
                this.injector.getInstance(EvaluationRepository.class),
                this.injector.getInstance(EvaluationRepository.class)
        );
    }

    @Test
    void shouldShareStudentAndRubricStorageWithFacade() {
        PosFacade facade = this.injector.getInstance(PosFacade.class);
        StudentRepository students = this.injector.getInstance(StudentRepository.class);
        RubricRepository rubrics = this.injector.getInstance(RubricRepository.class);
        Student student = facade.createStudent("Иван Иванов");
        Rubric rubric = facade.createRubric();

        assertSame(student, students.getById(student.getId()));
        assertSame(rubric, rubrics.getById(rubric.getId()));

        Student otherStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        Rubric otherRubric = new Rubric(UUID.randomUUID());
        students.save(otherStudent);
        rubrics.save(otherRubric);

        assertEquals(List.of(student, otherStudent), facade.findStudents());
        assertEquals(List.of(rubric, otherRubric), facade.findRubrics());
    }

    @Test
    void shouldCalculateFinalUsingSharedEvaluationStorage() {
        PosFacade facade = this.injector.getInstance(PosFacade.class);
        EvaluationApplicationService service = this.injector.getInstance(EvaluationApplicationService.class);
        EvaluationRepository evaluations = this.injector.getInstance(EvaluationRepository.class);
        Student student = facade.createStudent("Иван Иванов");
        Rubric rubric = facade.createRubric();
        Criterion exam = facade.addCriterion(rubric.getId(), "Экзамен", 10, new BigDecimal("0.60"));
        Criterion project = facade.addCriterion(rubric.getId(), "Проект", 20, new BigDecimal("0.40"));

        Evaluation examEvaluation = facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 8);
        Evaluation projectEvaluation = service.assignScore(student, rubric, project.getId(), 18);

        assertEquals(
                List.of(examEvaluation, projectEvaluation),
                evaluations.findByStudentAndRubric(student.getId(), rubric.getId())
        );
        assertEquals(
                List.of(examEvaluation, projectEvaluation),
                facade.findEvaluations(student.getId(), rubric.getId())
        );
        assertEquals(0, new BigDecimal("0.84").compareTo(
                facade.calculateFinal(student.getId(), rubric.getId())
        ));
        assertEquals(0, new BigDecimal("0.84").compareTo(service.calculateFinal(student, rubric)));
    }

    @Test
    void shouldExportResultWithConfiguredAdapter(@TempDir Path tempDir) throws IOException {
        PosFacade facade = this.injector.getInstance(PosFacade.class);
        Student student = facade.createStudent("Иван Иванов");
        Rubric rubric = facade.createRubric();
        Criterion exam = facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);
        facade.assignScore(student.getId(), rubric.getId(), exam.getId(), 8);
        Path destination = tempDir.resolve("results.csv");

        facade.exportResult(student.getId(), rubric.getId(), destination);

        assertEquals(
                "student_id,student_name,rubric_id,criterion_name,score,max_score,weight,final_percent\n"
                        + student.getId() + ",\"Иван Иванов\"," + rubric.getId() + ",\"Экзамен\",8,10,1,80\n",
                Files.readString(destination, StandardCharsets.UTF_8)
        );
        assertSame(
                this.injector.getInstance(ResultExporter.class),
                this.injector.getInstance(ResultExporter.class)
        );
    }

    @Test
    void shouldCreateConsoleMenu() {
        ConsoleMenu menu = this.injector.getInstance(ConsoleMenu.class);

        assertNotNull(menu);
        assertSame(menu, this.injector.getInstance(ConsoleMenu.class));
    }

    @Test
    void shouldKeepStorageSeparateBetweenInjectors() {
        this.injector.getInstance(PosFacade.class).createStudent("Иван Иванов");
        Injector otherInjector = Guice.createInjector(new PosModule());

        assertTrue(otherInjector.getInstance(PosFacade.class).findStudents().isEmpty());
        assertNotSame(
                this.injector.getInstance(StudentRepository.class),
                otherInjector.getInstance(StudentRepository.class)
        );
    }
}
