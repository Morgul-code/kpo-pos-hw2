package ru.kokoev.pos.infrastructure.persistence;

import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.repository.EvaluationRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryEvaluationRepository implements EvaluationRepository {
    private final Map<EvaluationKey, Evaluation> evaluations = new LinkedHashMap<>();

    @Override
    public void save(Evaluation evaluation) {
        EvaluationKey key = keyOf(evaluation);
        if (this.evaluations.containsKey(key)) {
            throw new IllegalArgumentException("Оценка по данному критерию уже выставлена");
        }
        this.evaluations.put(key, evaluation);
    }

    @Override
    public void update(Evaluation evaluation) {
        EvaluationKey key = keyOf(evaluation);
        Evaluation existing = this.evaluations.get(key);
        if (existing == null) {
            throw new IllegalArgumentException("Оценка по данному критерию не найдена");
        }
        if (!existing.getId().equals(evaluation.getId())) {
            throw new IllegalArgumentException("При обновлении нельзя изменять идентификатор оценки");
        }
        this.evaluations.put(key, evaluation);
    }

    @Override
    public void remove(UUID studentId, UUID rubricId, UUID criterionId) {
        EvaluationKey key = new EvaluationKey(studentId, rubricId, criterionId);
        if (this.evaluations.remove(key) == null) {
            throw new IllegalArgumentException("Оценка по данному критерию не найдена");
        }
    }

    @Override
    public List<Evaluation> findByStudentAndRubric(UUID studentId, UUID rubricId) {
        if (studentId == null || rubricId == null) {
            throw new IllegalArgumentException("Идентификаторы студента и рубрикатора не могут быть null");
        }
        List<Evaluation> result = new ArrayList<>();
        for (Evaluation evaluation : this.evaluations.values()) {
            if (evaluation.getStudentId().equals(studentId)
                    && evaluation.getRubricId().equals(rubricId)) {
                result.add(evaluation);
            }
        }
        return List.copyOf(result);
    }

    private EvaluationKey keyOf(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("Оценка не может быть null");
        }
        return new EvaluationKey(
                evaluation.getStudentId(), evaluation.getRubricId(), evaluation.getCriterionId()
        );
    }

    private record EvaluationKey(UUID studentId, UUID rubricId, UUID criterionId) {
        private EvaluationKey {
            if (studentId == null || rubricId == null || criterionId == null) {
                throw new IllegalArgumentException("Идентификаторы оценки не могут быть null");
            }
        }
    }
}
