package com.cultivar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public record CrossLog(List<Entry> entries) {
    public static final int MAX_ENTRIES = 256;

    public record Entry(Identifier species, String mother, String father, long day, String method, List<String> offspring) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("species").forGetter(Entry::species),
                Codec.STRING.fieldOf("mother").forGetter(Entry::mother),
                Codec.STRING.fieldOf("father").forGetter(Entry::father),
                Codec.LONG.fieldOf("day").forGetter(Entry::day),
                Codec.STRING.fieldOf("method").forGetter(Entry::method),
                Codec.STRING.listOf().fieldOf("offspring").forGetter(Entry::offspring)
        ).apply(i, Entry::new));
    }

    public static final MapCodec<CrossLog> MAP_CODEC = Entry.CODEC.listOf().fieldOf("entries").xmap(CrossLog::new, CrossLog::entries);

    public static CrossLog empty() {
        return new CrossLog(List.of());
    }

    public CrossLog with(Entry entry) {
        List<Entry> list = new ArrayList<>(entries);
        list.add(entry);
        while (list.size() > MAX_ENTRIES) {
            list.remove(0);
        }
        return new CrossLog(List.copyOf(list));
    }
}
