package ru.kokoev.pos.domain.model;

import ru.kokoev.pos.domain.model.Student;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    UUID id = UUID.randomUUID();


    @Test
    void shouldCreateStudentWithValidData() {
        Student student = new Student(id, "Иван Иванов");

        System.out.println(student.getFullName());
        assertEquals("Иван Иванов", student.getFullName());
        assertEquals(id, student.getId());
    }

    @Test
    void shouldRejectBlankFullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Student(UUID.randomUUID(), "   ")
        );
    }
}