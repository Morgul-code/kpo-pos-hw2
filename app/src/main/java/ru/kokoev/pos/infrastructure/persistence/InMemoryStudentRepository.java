package ru.kokoev.pos.infrastructure.persistence;

import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.StudentRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryStudentRepository implements StudentRepository {
    private final Map<UUID, Student> students = new LinkedHashMap<>();

    @Override
    public void save(Student student) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "Студент не может быть null"
            );
        }
        this.students.put(student.getId(), student);
    }

    @Override
    public Student getById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Идентификатор студента не может быть null"
            );
        }
        Student student = this.students.get(id);
        if (student == null) {
            throw new IllegalArgumentException(
                    "Студент с таким идентификатором не найден"
            );
        }
        return student;
    }

    @Override
    public List<Student> findAll() {
        return List.copyOf(this.students.values());
    }
}
