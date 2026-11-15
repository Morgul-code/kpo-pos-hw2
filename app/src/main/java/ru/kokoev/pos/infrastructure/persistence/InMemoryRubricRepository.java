package ru.kokoev.pos.infrastructure.persistence;

import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.repository.RubricRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryRubricRepository implements RubricRepository {
    private final Map<UUID, Rubric> rubrics = new LinkedHashMap<>();

    @Override
    public void save(Rubric rubric) {
        if (rubric == null) {
            throw new IllegalArgumentException(
                    "Рубрикатор не может быть null"
            );
        }
        this.rubrics.put(rubric.getId(), rubric);
    }

    @Override
    public Rubric getById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Идентификатор рубрикатора не может быть null"
            );
        }
        Rubric rubric = this.rubrics.get(id);
        if (rubric == null) {
            throw new IllegalArgumentException(
                    "Рубрикатор с таким идентификатором не найден"
            );
        }
        return rubric;
    }

    @Override
    public List<Rubric> findAll() {
        return List.copyOf(this.rubrics.values());
    }
}
