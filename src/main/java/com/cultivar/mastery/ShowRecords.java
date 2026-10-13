package com.cultivar.mastery;

import com.cultivar.Cultivar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class ShowRecords extends SavedData {
    public record Entry(int score, String breeder, long day) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("score").forGetter(Entry::score),
                Codec.STRING.fieldOf("breeder").forGetter(Entry::breeder),
                Codec.LONG.fieldOf("day").forGetter(Entry::day)
        ).apply(i, Entry::new));
    }

    public static final Codec<ShowRecords> CODEC = Codec.unboundedMap(Identifier.CODEC, Entry.CODEC).xmap(ShowRecords::new, r -> r.records);
    public static final SavedDataType<ShowRecords> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "show_records"), ShowRecords::new, CODEC);

    private final Map<Identifier, Entry> records;

    public ShowRecords() {
        this(Map.of());
    }

    private ShowRecords(Map<Identifier, Entry> records) {
        this.records = new HashMap<>(records);
    }

    public static ShowRecords get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<Entry> record(Identifier species) {
        return Optional.ofNullable(records.get(species));
    }

    public boolean submit(Identifier species, int score, String breeder, long day) {
        Entry current = records.get(species);
        if (current != null && current.score() >= score) {
            return false;
        }
        records.put(species, new Entry(score, breeder, day));
        setDirty();
        return true;
    }
}
