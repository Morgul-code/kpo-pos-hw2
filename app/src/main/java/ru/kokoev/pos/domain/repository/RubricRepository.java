package ru.kokoev.pos.domain.repository;

import ru.kokoev.pos.domain.model.Rubric;

import java.util.List;
import java.util.UUID;

public interface RubricRepository {
    /**
     * Сохраняет новый рубрикатор или повторно сохраняет тот же экземпляр.
     * Подмена сохранённого UUID другим объектом вызывает IllegalArgumentException.
     */
    void save(Rubric rubric);

    Rubric getById(UUID id);

    List<Rubric> findAll();
}
