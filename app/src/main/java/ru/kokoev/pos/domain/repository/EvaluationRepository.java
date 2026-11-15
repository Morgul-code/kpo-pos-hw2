package ru.kokoev.pos.domain.repository;

import ru.kokoev.pos.domain.model.Evaluation;

import java.util.List;
import java.util.UUID;

public interface EvaluationRepository {
    void save(Evaluation evaluation);

    List<Evaluation> findByStudentAndRubric(
            UUID studentId,
            UUID rubricId
    );
}
