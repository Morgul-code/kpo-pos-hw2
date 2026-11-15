package ru.kokoev.pos.domain.strategy;

import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;

import java.math.BigDecimal;
import java.util.List;

/**
 * Алгоритм расчёта для данных, проверенных FinalScoreService.
 */
public interface ScoringStrategy {
    BigDecimal calculate(Rubric rubric, List<Evaluation> evaluations);
}
