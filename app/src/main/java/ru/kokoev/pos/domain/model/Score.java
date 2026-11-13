package ru.kokoev.pos.domain.model;

public record Score(int value) {
    public Score {
        if (value < 0) {
            throw new IllegalArgumentException("Значение должно быть больше или равно нулю");
        }
    }
}
