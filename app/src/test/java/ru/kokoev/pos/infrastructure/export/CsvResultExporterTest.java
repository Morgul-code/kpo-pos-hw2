package ru.kokoev.pos.infrastructure.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.kokoev.pos.application.port.ResultExporter;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CsvResultExporterTest {
    private static final String HEADER =
            "student_id,student_name,rubric_id,criterion_name,score,max_score,weight,final_percent\n";
    private final ResultExporter exporter = new CsvResultExporter();
    private final Student student = new Student(UUID.randomUUID(), "Иван Иванов");
    private final Rubric rubric = new Rubric(UUID.randomUUID());
    private final Criterion exam = new Criterion(
            UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60")
    );
    private final Criterion project = new Criterion(
            UUID.randomUUID(), "Проект", 20, new BigDecimal("0.40")
    );

    public CsvResultExporterTest() {
        this.rubric.addCriterion(this.exam);
        this.rubric.addCriterion(this.project);
    }

    @Test
    void shouldExportValidCsv(@TempDir Path tempDir) throws IOException {
        Path destination = tempDir.resolve("results.csv");

        this.exporter.export(
                this.student, this.rubric,
                List.of(evaluation(this.project, 18), evaluation(this.exam, 8)),
                new BigDecimal("0.84"), destination
        );

        assertEquals(
                HEADER
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Экзамен\",8,10,0.60,84\n"
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Проект\",18,20,0.40,84\n",
                Files.readString(destination, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEscapeCommasAndQuotes(@TempDir Path tempDir) throws IOException {
        Student student = new Student(UUID.randomUUID(), "Иванов, \"Иван\"\r\nИванович");
        Rubric rubric = new Rubric(UUID.randomUUID());
        Criterion criterion = new Criterion(
                UUID.randomUUID(), "Проект, \"часть 1\"\nПродолжение", 10, BigDecimal.ONE
        );
        rubric.addCriterion(criterion);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), student, rubric, criterion.getId(), new Score(8)
        );
        Path destination = tempDir.resolve("results.csv");

        this.exporter.export(student, rubric, List.of(evaluation), new BigDecimal("0.8"), destination);

        assertEquals(
                HEADER + student.getId() + ",\"Иванов, \"\"Иван\"\"\r\nИванович\","
                        + rubric.getId() + ",\"Проект, \"\"часть 1\"\"\nПродолжение\",8,10,1,80\n",
                Files.readString(destination, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldExportMissingEvaluation(@TempDir Path tempDir) throws IOException {
        Path destination = tempDir.resolve("results.csv");

        this.exporter.export(
                this.student, this.rubric, List.of(evaluation(this.exam, 8)),
                new BigDecimal("0.48"), destination
        );

        assertEquals(
                HEADER
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Экзамен\",8,10,0.60,48\n"
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Проект\",,20,0.40,48\n",
                Files.readString(destination, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldDistinguishZeroScoreFromMissingEvaluation(@TempDir Path tempDir) throws IOException {
        Path destination = tempDir.resolve("results.csv");

        this.exporter.export(
                this.student, this.rubric, List.of(evaluation(this.exam, 0)),
                BigDecimal.ZERO, destination
        );

        assertEquals(
                HEADER
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Экзамен\",0,10,0.60,0\n"
                        + this.student.getId() + ",\"Иван Иванов\"," + this.rubric.getId()
                        + ",\"Проект\",,20,0.40,0\n",
                Files.readString(destination, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldUseProvidedFinalResult(@TempDir Path tempDir) throws IOException {
        Path destination = tempDir.resolve("results.csv");

        this.exporter.export(
                this.student, this.rubric,
                List.of(evaluation(this.exam, 8), evaluation(this.project, 18)),
                new BigDecimal("0.123"), destination
        );

        String[] lines = Files.readString(destination, StandardCharsets.UTF_8).split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[1].endsWith(",0.60,12.3"));
        assertTrue(lines[2].endsWith(",0.40,12.3"));
    }

    @Test
    void shouldRejectNullArguments(@TempDir Path tempDir) {
        Path destination = tempDir.resolve("results.csv");

        assertThrows(IllegalArgumentException.class,
                () -> this.exporter.export(null, this.rubric, List.of(), BigDecimal.ZERO, destination));
        assertThrows(IllegalArgumentException.class,
                () -> this.exporter.export(this.student, null, List.of(), BigDecimal.ZERO, destination));
        assertThrows(IllegalArgumentException.class,
                () -> this.exporter.export(this.student, this.rubric, null, BigDecimal.ZERO, destination));
        assertThrows(IllegalArgumentException.class,
                () -> this.exporter.export(this.student, this.rubric, List.of(), null, destination));
        assertThrows(IllegalArgumentException.class,
                () -> this.exporter.export(this.student, this.rubric, List.of(), BigDecimal.ZERO, null));
        assertFalse(Files.exists(destination));
    }

    @Test
    void shouldWrapWriteFailure(@TempDir Path tempDir) {
        UncheckedIOException exception = assertThrows(
                UncheckedIOException.class,
                () -> this.exporter.export(
                        this.student, this.rubric, List.of(), BigDecimal.ZERO, tempDir
                )
        );

        assertEquals("Не удалось сохранить CSV-файл", exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void shouldOverwriteExistingFile(@TempDir Path tempDir) throws IOException {
        Path destination = tempDir.resolve("results.csv");
        Files.writeString(destination, "Старое содержимое".repeat(100), StandardCharsets.UTF_8);

        this.exporter.export(
                this.student, this.rubric, List.of(evaluation(this.exam, 8)),
                new BigDecimal("0.48"), destination
        );

        String content = Files.readString(destination, StandardCharsets.UTF_8);
        assertTrue(content.startsWith(HEADER));
        assertEquals(3, content.split("\n").length);
        assertFalse(content.contains("Старое содержимое"));
    }

    private Evaluation evaluation(Criterion criterion, int points) {
        return new Evaluation(
                UUID.randomUUID(), this.student, this.rubric, criterion.getId(), new Score(points)
        );
    }
}
