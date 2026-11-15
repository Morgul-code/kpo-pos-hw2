package ru.kokoev.pos.domain.model;

import java.util.UUID;

public class Evaluation {
    private final UUID id;
    private final UUID studentId;
    private final UUID rubricId;
    private final UUID criterionId;
    private final Score score;

    public Evaluation(
            UUID id,
            Student student,
            Rubric rubric,
            UUID criterionId,
            Score score
    ) {
        if (id == null || student == null || rubric == null || criterionId == null || score == null) {
            throw new IllegalArgumentException(
                    "Аргументы оценки не могут быть null"
            );
        }

        Criterion criterion = rubric.getCriterionById(criterionId);
        if (score.value() > criterion.getMaxScore()) {
            throw new IllegalArgumentException(
                    "Балл не должен превышать максимум критерия"
            );
        }

        this.id = id;
        this.studentId = student.getId();
        this.rubricId = rubric.getId();
        this.criterionId = criterion.getId();
        this.score = score;
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getStudentId() {
        return this.studentId;
    }

    public UUID getRubricId() {
        return this.rubricId;
    }

    public UUID getCriterionId() {
        return this.criterionId;
    }

    public Score getScore() {
        return this.score;
    }
}
