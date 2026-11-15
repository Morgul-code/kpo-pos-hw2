package ru.kokoev.pos.domain.repository;

import ru.kokoev.pos.domain.model.Student;

import java.util.List;
import java.util.UUID;

public interface StudentRepository {
    void save(Student student);

    Student getById(UUID id);

    List<Student> findAll();
}
