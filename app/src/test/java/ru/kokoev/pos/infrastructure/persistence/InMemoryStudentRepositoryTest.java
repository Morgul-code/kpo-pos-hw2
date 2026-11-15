package ru.kokoev.pos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.StudentRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStudentRepositoryTest {
    private final StudentRepository repository = new InMemoryStudentRepository();
    private final Student student = new Student(UUID.randomUUID(), "Иван Иванов");

    @Test
    void shouldSaveAndFindStudent() {
        this.repository.save(this.student);

        assertSame(this.student, this.repository.getById(this.student.getId()));
    }

    @Test
    void shouldReturnAllStudents() {
        Student secondStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        this.repository.save(this.student);
        this.repository.save(secondStudent);

        assertEquals(List.of(this.student, secondStudent), this.repository.findAll());
    }

    @Test
    void shouldRejectUnknownStudentId() {
        this.repository.save(this.student);

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
        this.repository.save(this.student);
        List<Student> snapshot = this.repository.findAll();
        Student secondStudent = new Student(UUID.randomUUID(), "Пётр Петров");

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(secondStudent));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(this.student));
        this.repository.save(secondStudent);

        assertEquals(List.of(this.student), snapshot);
        assertEquals(List.of(this.student, secondStudent), this.repository.findAll());
    }

    @Test
    void shouldReplaceStudentWithSameId() {
        Student secondStudent = new Student(UUID.randomUUID(), "Пётр Петров");
        Student replacement = new Student(this.student.getId(), "Иван Петров");
        this.repository.save(this.student);
        this.repository.save(secondStudent);

        this.repository.save(replacement);

        assertSame(replacement, this.repository.getById(this.student.getId()));
        assertEquals(List.of(replacement, secondStudent), this.repository.findAll());
    }

    @Test
    void shouldRejectNullStudent() {
        assertThrows(IllegalArgumentException.class, () -> this.repository.save(null));
        assertTrue(this.repository.findAll().isEmpty());
    }

    @Test
    void shouldRejectNullStudentId() {
        assertThrows(IllegalArgumentException.class, () -> this.repository.getById(null));
    }
}
