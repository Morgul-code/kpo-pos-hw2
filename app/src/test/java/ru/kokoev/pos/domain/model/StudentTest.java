package test.java.ru.kokoev.pos.domain.model;

import main.java.ru.kokoev.pos.domain.model.Student;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    UUID id = UUID.randomUUID();


    @Test
    void shouldCreateStudentWithValidData() {
        Student student = new Student(id, "Иван Иванов");

        System.out.println(student.getFullName());
    }

    @Test
    void shouldRejectBlankFullName() {
        new Student(UUID.randomUUID(), "   ");
    }
}