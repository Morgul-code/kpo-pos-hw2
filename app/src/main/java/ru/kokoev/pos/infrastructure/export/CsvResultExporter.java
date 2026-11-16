package ru.kokoev.pos.infrastructure.export;

import ru.kokoev.pos.application.port.ResultExporter;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvResultExporter implements ResultExporter {
    @Override
    public void export(
            Student student,
            Rubric rubric,
            List<Evaluation> evaluations,
            BigDecimal finalResult,
            Path destination
    ) {
        if (student == null || rubric == null || evaluations == null
                || finalResult == null || destination == null) {
            throw new IllegalArgumentException("Аргументы экспорта не могут быть null");
        }

        String percentage = finalResult.multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros().toPlainString();
        StringBuilder csv = new StringBuilder(
                "student_id,student_name,rubric_id,criterion_name,score,max_score,weight,final_percent\n"
        );
        for (Criterion criterion : rubric.getCriteria()) {
            String score = "";
            for (Evaluation evaluation : evaluations) {
                if (criterion.getId().equals(evaluation.getCriterionId())) {
                    score = Integer.toString(evaluation.getScore().value());
                    break;
                }
            }
            csv.append(student.getId()).append(',')
                    .append(escapeCsv(student.getFullName())).append(',')
                    .append(rubric.getId()).append(',')
                    .append(escapeCsv(criterion.getName())).append(',')
                    .append(score).append(',')
                    .append(criterion.getMaxScore()).append(',')
                    .append(criterion.getWeight().toPlainString()).append(',')
                    .append(percentage).append('\n');
        }

        try {
            Files.writeString(destination, csv, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Не удалось сохранить CSV-файл", exception);
        }
    }

    private String escapeCsv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
