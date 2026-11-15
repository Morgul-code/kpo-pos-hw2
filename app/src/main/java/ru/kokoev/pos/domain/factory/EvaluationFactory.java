package ru.kokoev.pos.domain.factory;

import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;

import java.math.BigDecimal;
import java.util.UUID;

public class EvaluationFactory {
    public Evaluation create(
            Student student,
            Rubric rubric,
            UUID criterionId,
            int points
    ) {
        if (student == null || rubric == null || criterionId == null) {
            throw new IllegalArgumentException(
                    "Аргументы создания оценки не могут быть null"
            );
        }
        if (rubric.getTotalWeight().compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException(
                    "Нельзя выставлять оценки: сумма весов рубрикатора должна равняться 1"
            );
        }

        Score score = new Score(points);
        return new Evaluation(UUID.randomUUID(), student, rubric, criterionId, score);
    }
}
