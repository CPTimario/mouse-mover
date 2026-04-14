package io.github.cptimario.mousemover;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Instant;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class IdleDetectorTest {

    @Test
    public void testIdleBelowThreshold() {
        // provider reports 10 seconds idle
        IdleTimeProvider provider = new IdleTimeProvider() {
            @Override public long getIdleTimeSeconds() { return 10L; }
        };
        IdleDetector detector = new IdleDetector(provider, 30, 5, 5, false,50, false, new Random(0));
        Instant lastMove = Instant.now().minusSeconds(1000);
        Point pos = new Point(500,500);
        Dimension screen = new Dimension(1000,1000);

        IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
        assertFalse(d.shouldMove, "Should not move when idle time is below threshold");
    }

    @Test
    public void testGraceWindowSkipsAndAllows() {
        // deterministic Random that always returns 3 for nextInt(bound)
        Random fixed = new Random() {
            @Override
            public int nextInt(int bound) { return Math.min(3, Math.max(0, bound-1)); }
        };

        // provider that reports a configurable idle time via closure
        class P implements IdleTimeProvider {
            long v;
            P(long v) { this.v = v; }
            @Override public long getIdleTimeSeconds() { return v; }
        }

        IdleDetector detector = new IdleDetector(new P(7), 5, 5, 1, false,50, false, fixed);
        Instant lastMove = Instant.now().minusSeconds(1000);
        Point pos = new Point(500,500);
        Dimension screen = new Dimension(1000,1000);

        // idleTime = 7 (< 5 + 3) -> skip
        IdleDetector.IdleDecision d1 = detector.evaluate(lastMove, pos, screen);
        assertFalse(d1.shouldMove, "Should skip within randomized grace window");

        // idleTime = 8 (== 5 + 3) -> allow
        detector = new IdleDetector(new P(8), 5, 5, 1, false,50, false, fixed);
        IdleDetector.IdleDecision d2 = detector.evaluate(lastMove, pos, screen);
        assertTrue(d2.shouldMove, "Should allow when idleTime meets idle+grace");
    }

    @Test
    public void testCooldownSkips() {
        IdleTimeProvider provider = new IdleTimeProvider() { @Override public long getIdleTimeSeconds() { return 100L; } };
        IdleDetector detector = new IdleDetector(provider, 5, 0, 10, false,50, false, new Random(0));
        Instant lastMove = Instant.now(); // just moved
        Point pos = new Point(500,500);
        Dimension screen = new Dimension(1000,1000);

        IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
        assertFalse(d.shouldMove, "Should skip due to cooldown since last move");
    }

    @Test
    public void testEdgeSuppression() {
        IdleTimeProvider provider = new IdleTimeProvider() { @Override public long getIdleTimeSeconds() { return 100L; } };
        IdleDetector detector = new IdleDetector(provider, 5, 0, 1, false,50, false, new Random(0));
        Instant lastMove = Instant.now().minusSeconds(1000);
        // position near left edge
        Point pos = new Point(10, 500);
        Dimension screen = new Dimension(1000,1000);

        IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
        assertFalse(d.shouldMove, "Should skip when mouse is near edge");
    }
}

