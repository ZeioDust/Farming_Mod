package com.cultivar.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

public record GuideState(boolean manualGiven, List<String> hintsSeen, List<String> milestones, boolean hintsOff) {
    public static final MapCodec<GuideState> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.BOOL.optionalFieldOf("manual_given", false).forGetter(GuideState::manualGiven),
            Codec.STRING.listOf().optionalFieldOf("hints_seen", List.of()).forGetter(GuideState::hintsSeen),
            Codec.STRING.listOf().optionalFieldOf("milestones", List.of()).forGetter(GuideState::milestones),
            Codec.BOOL.optionalFieldOf("hints_off", false).forGetter(GuideState::hintsOff)
    ).apply(i, GuideState::new));

    public static GuideState empty() {
        return new GuideState(false, List.of(), List.of(), false);
    }

    public boolean seen(String hint) {
        return hintsSeen.contains(hint);
    }

    public boolean done(String milestone) {
        return milestones.contains(milestone);
    }

    public GuideState withManualGiven() {
        return new GuideState(true, hintsSeen, milestones, hintsOff);
    }

    public GuideState withHintSeen(String hint) {
        if (seen(hint)) {
            return this;
        }
        List<String> next = new ArrayList<>(hintsSeen);
        next.add(hint);
        return new GuideState(manualGiven, List.copyOf(next), milestones, hintsOff);
    }

    public GuideState withMilestone(String milestone) {
        if (done(milestone)) {
            return this;
        }
        List<String> next = new ArrayList<>(milestones);
        next.add(milestone);
        return new GuideState(manualGiven, hintsSeen, List.copyOf(next), hintsOff);
    }

    public GuideState withHintsOff(boolean off) {
        return new GuideState(manualGiven, hintsSeen, milestones, off);
    }
}
