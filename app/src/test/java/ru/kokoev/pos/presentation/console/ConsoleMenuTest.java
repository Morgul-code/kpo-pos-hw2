package ru.kokoev.pos.presentation.console;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.application.facade.PosFacade;
import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryRubricRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryStudentRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleMenuTest {
    private final PosFacade facade = new PosFacade(
            new InMemoryStudentRepository(),
            new InMemoryRubricRepository(),
            new EvaluationApplicationService(new InMemoryEvaluationRepository(), new FinalScoreService())
    );

    @Test
    void shouldCreateDataAssignScoresAndCalculateFinal() {
        String output = runMenu("""
                1
                Иван Иванов
                3
                5
                1
                Экзамен
                10
                0,60
                5
                1
                Проект
                20
                0.40
                7
                1
                1
                1
                8
                7
                1
                1
                2
                18
                8
                1
                1
                0
                """);

        assertTrue(output.contains("Итоговый результат: 84%"));
        assertTrue(output.contains("Студент: Иван Иванов"));
        assertTrue(output.contains("До свидания!"));
        assertFalse(output.contains("Ошибка:"));
        Student student = this.facade.findStudents().get(0);
        Rubric rubric = this.facade.findRubrics().get(0);
        assertEquals(2, this.facade.findCriteria(rubric.getId()).size());
        assertEquals(2, this.facade.findEvaluations(student.getId(), rubric.getId()).size());
    }

    @Test
    void shouldDisplaySavedData() {
        this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);

        String output = runMenu("2\n4\n6\n1\n0\n");

        assertTrue(output.contains("Иван Иванов"));
        assertTrue(output.contains(rubric.getId().toString()));
        assertTrue(output.contains("Экзамен — максимум 10, вес 1"));
    }

    @Test
    void shouldReportEmptyLists() {
        String output = runMenu("2\n4\n5\n7\n0\n");

        assertTrue(output.contains("Студентов пока нет."));
        assertTrue(output.contains("Рубрикаторов пока нет."));
        assertTrue(output.contains("Сначала создайте рубрикатор."));
        assertTrue(output.contains("Сначала добавьте студента."));
        assertTrue(output.contains("До свидания!"));
    }

    @Test
    void shouldRejectInvalidSelectionAndContinue() {
        this.facade.createRubric();

        String output = runMenu("6\nabc\n6\n2\n0\n");

        assertTrue(output.contains("Введите целое число."));
        assertTrue(output.contains("Номер должен быть от 1 до 1."));
        assertTrue(output.contains("До свидания!"));
    }

    @Test
    void shouldReportDuplicateScoreWithoutOverwriting() {
        Student student = this.facade.createStudent("Иван Иванов");
        Rubric rubric = this.facade.createRubric();
        Criterion criterion = this.facade.addCriterion(rubric.getId(), "Экзамен", 10, BigDecimal.ONE);
        this.facade.assignScore(student.getId(), rubric.getId(), criterion.getId(), 8);

        String output = runMenu("7\n1\n1\n1\n9\n0\n");

        assertTrue(output.contains("Оценка по данному критерию уже выставлена"));
        assertEquals(1, this.facade.findEvaluations(student.getId(), rubric.getId()).size());
        assertEquals(8, this.facade.findEvaluations(student.getId(), rubric.getId())
                .get(0).getScore().value());
    }

    @Test
    void shouldReportUnknownCommand() {
        String output = runMenu("unknown\n0\n");

        assertTrue(output.contains("Неизвестная команда."));
        assertTrue(output.contains("До свидания!"));
    }

    @Test
    void shouldExitAtEndOfInput() {
        assertTrue(runMenu("").contains("Ввод завершён."));
        assertTrue(runMenu("1\n").contains("Ввод завершён."));
        assertTrue(this.facade.findStudents().isEmpty());
    }

    private String runMenu(String commands) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(commands);
             PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new ConsoleMenu(this.facade, input, output).run();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
