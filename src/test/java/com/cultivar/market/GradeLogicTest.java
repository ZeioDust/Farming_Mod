package com.cultivar.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cultivar.processing.RationData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class GradeLogicTest {
    @Test
    void thresholdsSplitTheScale() {
        assertEquals(Grade.C, GradeLogic.of(0f));
        assertEquals(Grade.C, GradeLogic.of(0.4199f));
        assertEquals(Grade.B, GradeLogic.of(0.42f));
        assertEquals(Grade.B, GradeLogic.of(0.5799f));
        assertEquals(Grade.A, GradeLogic.of(0.58f));
        assertEquals(Grade.A, GradeLogic.of(0.7399f));
        assertEquals(Grade.PRIME, GradeLogic.of(0.74f));
        assertEquals(Grade.PRIME, GradeLogic.of(1f));
    }

    @Test
    void scoreBlendsGenesAndCare() {
        assertEquals(GradeLogic.GENETIC_WEIGHT, GradeLogic.score(1f, 0f), 1e-6f);
        assertEquals(1f - GradeLogic.GENETIC_WEIGHT, GradeLogic.score(0f, 1f), 1e-6f);
        assertEquals(1f, GradeLogic.score(1f, 1f), 1e-6f);
        assertEquals(Grade.B, GradeLogic.of(GradeLogic.score(1f, 0f)));
        assertEquals(Grade.B, GradeLogic.of(GradeLogic.score(0f, 1f)));
    }

    @Test
    void stressLowersTheGrowingScore() {
        assertEquals(0.9f, GradeLogic.growingScore(0.9f, 0), 1e-6f);
        assertEquals(0.9f - 0.08f * 3, GradeLogic.growingScore(0.9f, 3), 1e-6f);
        assertEquals(0f, GradeLogic.growingScore(0.2f, 10), 1e-6f);
        assertEquals(1f, GradeLogic.growingScore(1.4f, 0), 1e-6f);
    }

    @Test
    void higherGradesPayAndFeedMore() {
        Grade[] grades = Grade.values();
        for (int i = 1; i < grades.length; i++) {
            assertTrue(grades[i].priceMultiplier() > grades[i - 1].priceMultiplier());
            assertTrue(grades[i].foodMultiplier() > grades[i - 1].foodMultiplier());
            assertTrue(grades[i].atLeast(grades[i - 1]));
        }
        assertEquals(1f, Grade.C.priceMultiplier());
        assertEquals(4f, Grade.PRIME.priceMultiplier());
    }

    @Test
    void rationStrengthCountsVarietyAndGrade() {
        List<Identifier> one = List.of(Identifier.fromNamespaceAndPath("cultivar", "maize"));
        assertEquals(1, new RationData(one, Grade.C).strength());
        assertEquals(4, new RationData(one, Grade.PRIME).strength());
        List<Identifier> many = new ArrayList<>();
        for (String s : new String[]{"wheatline", "maize", "soybean", "clover", "tuber", "brassica", "gourd", "fiber_flax"}) {
            many.add(Identifier.fromNamespaceAndPath("cultivar", s));
        }
        assertEquals(7, new RationData(many, Grade.PRIME).strength());
        assertEquals(7, new RationData(many, Grade.C).strength());
        assertEquals(5, new RationData(many.subList(0, 4), Grade.B).strength());
    }
}
