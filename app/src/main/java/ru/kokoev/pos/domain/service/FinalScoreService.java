package ru.kokoev.pos.domain.service;

import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FinalScoreService {
    public BigDecimal calculate(
            UUID studentId,
            Rubric rubric,
            List<Evaluation> evaluations
    ) {
        if (studentId == null || rubric == null || evaluations == null) {
            throw new IllegalArgumentException(
                    "Аргументы расчёта не могут быть null"
            );
        }
        if (rubric.getTotalWeight().compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException(
                    "Сумма весов рубрикатора должна равняться единице"
            );
        }

        Set<UUID> evaluatedCriterionIds = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Evaluation evaluation : evaluations) {
            if (evaluation == null) {
                throw new IllegalArgumentException(
                        "Список оценок не может содержать null"
                );
            }
            if (!studentId.equals(evaluation.getStudentId())
                    || !rubric.getId().equals(evaluation.getRubricId())) {
                throw new IllegalArgumentException(
                        "Оценка должна принадлежать указанному студенту и рубрикатору"
                );
            }

            Criterion criterion = rubric.getCriterionById(evaluation.getCriterionId());
            if (!evaluatedCriterionIds.add(criterion.getId())) {
                throw new IllegalArgumentException(
                        "Повторная оценка одного критерия недопустима"
                );
            }
            if (evaluation.getScore().value() > criterion.getMaxScore()) {
                throw new IllegalArgumentException(
                        "Балл не должен превышать максимум критерия"
                );
            }

            BigDecimal ratio = BigDecimal.valueOf(evaluation.getScore().value()).divide(
                    BigDecimal.valueOf(criterion.getMaxScore()),
                    10,
                    RoundingMode.HALF_UP
            );
            total = total.add(ratio.multiply(criterion.getWeight()));
        }
        return total;
    }
}
