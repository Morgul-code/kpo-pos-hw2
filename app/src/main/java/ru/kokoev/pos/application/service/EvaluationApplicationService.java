package ru.kokoev.pos.application.service;

import ru.kokoev.pos.domain.factory.EvaluationFactory;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class EvaluationApplicationService {
    private final EvaluationRepository repository;
    private final FinalScoreService finalScoreService;
    private final EvaluationFactory evaluationFactory;

    public EvaluationApplicationService(
            EvaluationRepository repository,
            FinalScoreService finalScoreService,
            EvaluationFactory evaluationFactory
    ) {
        if (repository == null || finalScoreService == null || evaluationFactory == null) {
            throw new IllegalArgumentException(
                    "Зависимости сервиса не могут быть null"
            );
        }
        this.repository = repository;
        this.finalScoreService = finalScoreService;
        this.evaluationFactory = evaluationFactory;
    }

    public Evaluation assignScore(
            Student student,
            Rubric rubric,
            UUID criterionId,
            int points
    ) {
        if (student == null || rubric == null || criterionId == null) {
            throw new IllegalArgumentException(
                    "Аргументы выставления оценки не могут быть null"
            );
        }

        List<Evaluation> existing = this.repository.findByStudentAndRubric(
                student.getId(), rubric.getId()
        );
        for (Evaluation evaluation : existing) {
            if (evaluation.getCriterionId().equals(criterionId)) {
                throw new IllegalArgumentException(
                        "Оценка по данному критерию уже выставлена"
                );
            }
        }

        Evaluation newEvaluation = this.evaluationFactory.create(
                student, rubric, criterionId, points
        );
        this.repository.save(newEvaluation);
        return newEvaluation;
    }

    public Evaluation updateScore(Student student, Rubric rubric, UUID criterionId, int points) {
        Evaluation existing = getExistingEvaluation(student, rubric, criterionId);
        Evaluation updated = this.evaluationFactory.create(
                existing.getId(), student, rubric, criterionId, points
        );
        this.repository.update(updated);
        return updated;
    }

    public void removeScore(Student student, Rubric rubric, UUID criterionId) {
        getExistingEvaluation(student, rubric, criterionId);
        this.repository.remove(student.getId(), rubric.getId(), criterionId);
    }

    private Evaluation getExistingEvaluation(Student student, Rubric rubric, UUID criterionId) {
        if (criterionId == null) {
            throw new IllegalArgumentException("Идентификатор критерия не может быть null");
        }
        for (Evaluation evaluation : findEvaluations(student, rubric)) {
            if (evaluation.getCriterionId().equals(criterionId)) {
                return evaluation;
            }
        }
        throw new IllegalArgumentException("Оценка по данному критерию не найдена");
    }

    public List<Evaluation> findEvaluations(
            Student student,
            Rubric rubric
    ) {
        if (student == null || rubric == null) {
            throw new IllegalArgumentException(
                    "Студент и рубрикатор не могут быть null"
            );
        }
        return this.repository.findByStudentAndRubric(student.getId(), rubric.getId());
    }

    public BigDecimal calculateFinal(
            Student student,
            Rubric rubric
    ) {
        List<Evaluation> evaluations = findEvaluations(student, rubric);
        return this.finalScoreService.calculate(student.getId(), rubric, evaluations);
    }
}
