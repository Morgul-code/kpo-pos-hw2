package ru.kokoev.pos.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Rubric {
    private final UUID id;
    private final List<Criterion> criteria = new ArrayList<>();

    public Rubric(UUID id) {
        this.id = Objects.requireNonNull(id, "ID не задано");
    }

    public void addCriterion(Criterion criterion) {
        if (criterion == null) {
            throw new IllegalArgumentException(
                    "Критерий не может быть null"
            );
        }

        for (Criterion searchableCriterion : criteria) {
            if (searchableCriterion.getId().equals(criterion.getId())) {
                throw new IllegalArgumentException(
                        "Критерий с таким идентификатором уже существует"
                );
            }
        }

        BigDecimal newTotalWeight =
                getTotalWeight().add(criterion.getWeight());
        if (newTotalWeight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Сумма весов не должна превышать единицу!"
            );
        }

        this.criteria.add(criterion);
    }

    public Criterion getCriterionById(UUID criterionId) {
        for (Criterion criterion : criteria) {
            if (criterion.getId().equals(criterionId)) {
                return criterion;
            }
        }
        throw new IllegalArgumentException(
                "Критерий с таким идентификатором не найден"
        );
    }

    public BigDecimal getTotalWeight() {
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Criterion tempCriterion : criteria) {
            totalWeight = totalWeight.add(tempCriterion.getWeight());
        }
        return totalWeight;
    }

    public List<Criterion> getCriteria() {
        return List.copyOf(this.criteria);
    }

    public UUID getId() {
        return this.id;
    }
}
