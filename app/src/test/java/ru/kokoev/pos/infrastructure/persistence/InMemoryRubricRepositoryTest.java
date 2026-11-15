package ru.kokoev.pos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.repository.RubricRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryRubricRepositoryTest {
    private final RubricRepository repository = new InMemoryRubricRepository();
    private final Rubric rubric = new Rubric(UUID.randomUUID());

    @Test
    void shouldSaveAndFindRubric() {
        Criterion criterion = new Criterion(
                UUID.randomUUID(), "Экзамен", 10, BigDecimal.ONE
        );
        this.rubric.addCriterion(criterion);
        this.repository.save(this.rubric);

        Rubric found = this.repository.getById(this.rubric.getId());

        assertSame(this.rubric, found);
        assertSame(criterion, found.getCriterionById(criterion.getId()));
    }

    @Test
    void shouldReturnAllRubrics() {
        Rubric secondRubric = new Rubric(UUID.randomUUID());
        this.repository.save(this.rubric);
        this.repository.save(secondRubric);

        assertEquals(List.of(this.rubric, secondRubric), this.repository.findAll());
    }

    @Test
    void shouldRejectUnknownRubricId() {
        this.repository.save(this.rubric);

        assertThrows(
                IllegalArgumentException.class,
                () -> this.repository.getById(UUID.randomUUID())
        );
    }

    @Test
    void shouldReturnEmptyListInitially() {
        assertTrue(this.repository.findAll().isEmpty());
    }

    @Test
    void shouldReturnUnmodifiableSnapshot() {
        this.repository.save(this.rubric);
        List<Rubric> snapshot = this.repository.findAll();
        Rubric secondRubric = new Rubric(UUID.randomUUID());

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(secondRubric));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(this.rubric));
        this.repository.save(secondRubric);

        assertEquals(List.of(this.rubric), snapshot);
        assertEquals(List.of(this.rubric, secondRubric), this.repository.findAll());
    }

    @Test
    void shouldReplaceRubricWithSameId() {
        Rubric secondRubric = new Rubric(UUID.randomUUID());
        Rubric replacement = new Rubric(this.rubric.getId());
        this.repository.save(this.rubric);
        this.repository.save(secondRubric);

        this.repository.save(replacement);

        assertSame(replacement, this.repository.getById(this.rubric.getId()));
        assertEquals(List.of(replacement, secondRubric), this.repository.findAll());
    }

    @Test
    void shouldRejectNullRubric() {
        assertThrows(IllegalArgumentException.class, () -> this.repository.save(null));
        assertTrue(this.repository.findAll().isEmpty());
    }

    @Test
    void shouldRejectNullRubricId() {
        assertThrows(IllegalArgumentException.class, () -> this.repository.getById(null));
    }
}
