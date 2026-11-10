package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {


    @Test
    void shouldCreateStudentWithValidData() {
        UUID id = UUID.randomUUID();

        Student student = new Student(id, "Иван Иванов");

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