package ru.kokoev.pos.domain.model;

import java.util.UUID;

public class Student {
    private final UUID id;
    private final String fullName;

    public Student(UUID id, String fullName) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Error: Identifier (ID) cannot be null"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Error: Name cannot be empty or consist only of spaces."
            );
        }

        this.id = id;
        this.fullName = fullName;
    }

    public UUID getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }
}