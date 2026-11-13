package ru.kokoev.pos.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Rubric {
    private final UUID id;
    private final List<Criterion> criteria;

    public Rubric(UUID id, List<Criterion> criteria) {
        this.id = Objects.requireNonNull(id, "ID не задано");
        this.criteria = List.copyOf(Objects.requireNonNull(criteria, "Criteria cannot be null"));
    }

    public void addCriterion(Criterion criterion) {
        BigDecimal tempForWeight = BigDecimal.ZERO;
        Objects.requireNonNull(criterion, "Критерий не задан");

        for (Criterion searchableCriterion : criteria) {
            if (searchableCriterion.getId() == criterion.getId()) {
                throw new IllegalArgumentException(
                        "Данная критерия уже присутствует в списке!"
                );
            }
        }

        tempForWeight = tempForWeight.add(getTotalWeight());
        tempForWeight = tempForWeight.add(criterion.getWeight());
        if (tempForWeight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Сумма весов не должна превышать единицу!"
            );
        }

        this.criteria.add(criterion);
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

