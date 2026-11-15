package ru.kokoev.pos.domain.strategy;

import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class WeightedScoringStrategy implements ScoringStrategy {
    @Override
    public BigDecimal calculate(Rubric rubric, List<Evaluation> evaluations) {
        BigDecimal total = BigDecimal.ZERO;
        for (Evaluation evaluation : evaluations) {
            Criterion criterion = rubric.getCriterionById(evaluation.getCriterionId());
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
