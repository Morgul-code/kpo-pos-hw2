package ru.kokoev.pos.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ScoreTest {
    @Test
    void shouldAcceptValidScore(){
        assertDoesNotThrow(
                () -> new Score(5)
        );
    }

    @Test
    void shouldRejectInvalidScore(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Score(-5)
        );
    }

    @Test
    void shouldAcceptEqualScores(){
        Score first = new Score(8);
        Score second = new Score(8);

        assertEquals(first, second);
    }

    @Test
    void shouldAcceptZeroScore() {
        Score score = new Score(0);

        assertEquals(0, score.value());
    }
}
