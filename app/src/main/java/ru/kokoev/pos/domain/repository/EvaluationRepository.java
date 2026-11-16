package ru.kokoev.pos.domain.repository;

import ru.kokoev.pos.domain.model.Evaluation;

import java.util.List;
import java.util.UUID;

public interface EvaluationRepository {
    /**
     * Добавляет новую оценку. Повторный ключ (студент, рубрикатор, критерий)
     * отклоняется с IllegalArgumentException, даже если UUID оценки совпадает.
     */
    void save(Evaluation evaluation);

    /**
     * Заменяет существующую оценку с тем же ключом и UUID.
     * Несуществующая запись или смена UUID вызывают IllegalArgumentException.
     */
    void update(Evaluation evaluation);

    /**
     * Удаляет оценку по составному ключу.
     * Если записи нет, выбрасывает IllegalArgumentException.
     */
    void remove(UUID studentId, UUID rubricId, UUID criterionId);

    List<Evaluation> findByStudentAndRubric(
            UUID studentId,
            UUID rubricId
    );
}
