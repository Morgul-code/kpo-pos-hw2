package ru.kokoev.pos.application.port;

import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

public interface ResultExporter {
    void export(
            Student student,
            Rubric rubric,
            List<Evaluation> evaluations,
            BigDecimal finalResult,
            Path destination
    );
}
