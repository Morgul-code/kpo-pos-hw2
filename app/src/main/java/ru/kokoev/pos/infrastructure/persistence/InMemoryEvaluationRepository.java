package ru.kokoev.pos.infrastructure.persistence;

import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.repository.EvaluationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryEvaluationRepository implements EvaluationRepository {
    private final List<Evaluation> evaluations = new ArrayList<>();

    @Override
    public void save(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException(
                    "Оценка не может быть null"
            );
        }
        this.evaluations.add(evaluation);
    }

    @Override
    public List<Evaluation> findByStudentAndRubric(UUID studentId, UUID rubricId) {
        List<Evaluation> result = new ArrayList<>();
        for (Evaluation evaluation : this.evaluations) {
            if (evaluation.getStudentId().equals(studentId)
                    && evaluation.getRubricId().equals(rubricId)) {
                result.add(evaluation);
            }
        }
        return List.copyOf(result);
    }
}
