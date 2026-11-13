package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ScoreTest {
    @Test
    void ShouldAcceptValidScore(){
        assertDoesNotThrow(
                () -> new Score(5)
        );
    }

    @Test
    void ShouldRejectInvalidScore(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Score(-5)
        );
    }

    @Test
    void ShouldAcceptEqualScores(){
        Score first = new Score(8);
        Score second = new Score(8);

        assertEquals(first, second);
    }
}
