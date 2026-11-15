package ru.kokoev.pos.domain.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Score;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.service.FinalScoreService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ScoringStrategyTest {
    private final Student student = new Student(UUID.randomUUID(), "Иван Иванов");
    private final Rubric rubric = new Rubric(UUID.randomUUID());
    private final Criterion exam = new Criterion(
            UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60")
    );
    private final Criterion project = new Criterion(
            UUID.randomUUID(), "Проект", 20, new BigDecimal("0.40")
    );

    public ScoringStrategyTest() {
        this.rubric.addCriterion(this.exam);
        this.rubric.addCriterion(this.project);
    }

    static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of(new WeightedScoringStrategy(), "0.84", "0.48"),
                Arguments.of(new EqualWeightScoringStrategy(), "0.85", "0.40")
        );
    }

    static Stream<ScoringStrategy> strategies() {
        return Stream.of(new WeightedScoringStrategy(), new EqualWeightScoringStrategy());
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldCalculateUsingSelectedStrategy(
            ScoringStrategy strategy, String expectedScore, String expectedMissingScore
    ) {
        FinalScoreService service = new FinalScoreService(strategy);
        BigDecimal result = service.calculate(
                this.student.getId(), this.rubric,
                List.of(evaluation(this.exam, 8), evaluation(this.project, 18))
        );

        assertEquals(0, new BigDecimal(expectedScore).compareTo(result));

        BigDecimal resultWithMissingEvaluation = service.calculate(
                this.student.getId(), this.rubric, List.of(evaluation(this.exam, 8))
        );
        assertEquals(0, new BigDecimal(expectedMissingScore).compareTo(resultWithMissingEvaluation));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void shouldReturnZeroForEmptyEvaluations(ScoringStrategy strategy) {
        assertEquals(0, BigDecimal.ZERO.compareTo(strategy.calculate(this.rubric, List.of())));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void shouldReturnZeroForZeroScores(ScoringStrategy strategy) {
        BigDecimal result = strategy.calculate(
                this.rubric, List.of(evaluation(this.exam, 0), evaluation(this.project, 0))
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void shouldReturnOneForMaximumScores(ScoringStrategy strategy) {
        BigDecimal result = strategy.calculate(
                this.rubric, List.of(evaluation(this.exam, 10), evaluation(this.project, 20))
        );

        assertEquals(0, BigDecimal.ONE.compareTo(result));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void shouldRoundNonTerminatingRatio(ScoringStrategy strategy) {
        Rubric rubric = new Rubric(UUID.randomUUID());
        Criterion criterion = new Criterion(UUID.randomUUID(), "Задание", 3, BigDecimal.ONE);
        rubric.addCriterion(criterion);
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, rubric, criterion.getId(), new Score(2)
        );

        BigDecimal result = strategy.calculate(rubric, List.of(evaluation));

        assertEquals(0, new BigDecimal("0.6666666667").compareTo(result));
    }

    @Test
    void shouldRoundEqualWeightAverageOverAllCriteria() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        Criterion first = new Criterion(UUID.randomUUID(), "Первое", 10, new BigDecimal("0.20"));
        rubric.addCriterion(first);
        rubric.addCriterion(new Criterion(UUID.randomUUID(), "Второе", 10, new BigDecimal("0.30")));
        rubric.addCriterion(new Criterion(UUID.randomUUID(), "Третье", 10, new BigDecimal("0.50")));
        Evaluation evaluation = new Evaluation(
                UUID.randomUUID(), this.student, rubric, first.getId(), new Score(10)
        );

        BigDecimal result = new EqualWeightScoringStrategy().calculate(rubric, List.of(evaluation));

        assertEquals(0, new BigDecimal("0.3333333333").compareTo(result));
    }

    private Evaluation evaluation(Criterion criterion, int points) {
        return new Evaluation(
                UUID.randomUUID(), this.student, this.rubric, criterion.getId(), new Score(points)
        );
    }
}
