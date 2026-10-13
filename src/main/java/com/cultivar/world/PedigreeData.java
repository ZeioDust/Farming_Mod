package com.cultivar.world;

import com.cultivar.Cultivar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PedigreeData extends SavedData {
    public enum Kind {
        NATURAL, WILD, CROSS, SELF, MUTANT, BANKED, EDITED;

        public static Kind byName(String name) {
            for (Kind kind : values()) {
                if (kind.name().equals(name)) {
                    return kind;
                }
            }
            return NATURAL;
        }
    }

    public record Record(long id, Identifier species, long mother, long father, String kind, long gameTime, String genomeHex) {
        public static final Codec<Record> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("id").forGetter(Record::id),
                Identifier.CODEC.fieldOf("species").forGetter(Record::species),
                Codec.LONG.fieldOf("mother").forGetter(Record::mother),
                Codec.LONG.fieldOf("father").forGetter(Record::father),
                Codec.STRING.fieldOf("kind").forGetter(Record::kind),
                Codec.LONG.fieldOf("time").forGetter(Record::gameTime),
                Codec.STRING.fieldOf("genome").forGetter(Record::genomeHex)
        ).apply(i, Record::new));
    }

    public static final Codec<PedigreeData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.fieldOf("next_id").forGetter(d -> d.nextId),
            Record.CODEC.listOf().fieldOf("records").forGetter(d -> new ArrayList<>(d.records.values()))
    ).apply(i, PedigreeData::new));

    public static final SavedDataType<PedigreeData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "pedigree"), PedigreeData::new, CODEC);

    private long nextId;
    private final Map<Long, Record> records = new HashMap<>();

    public PedigreeData() {
        this(1L, List.of());
    }

    private PedigreeData(long nextId, List<Record> list) {
        this.nextId = Math.max(1L, nextId);
        for (Record record : list) {
            records.put(record.id(), record);
        }
    }

    public static PedigreeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public long create(Identifier species, long mother, long father, Kind kind, long gameTime, String genomeHex) {
        long id = nextId++;
        records.put(id, new Record(id, species, mother, father, kind.name(), gameTime, genomeHex));
        setDirty();
        return id;
    }

    public Record get(long id) {
        return records.get(id);
    }

    public int size() {
        return records.size();
    }

    public Map<Long, Record> snapshotAncestry(long root, int depth) {
        Map<Long, Record> out = new HashMap<>();
        collect(root, depth, out);
        return out;
    }

    private void collect(long id, int depth, Map<Long, Record> out) {
        if (depth < 0 || id <= 0 || out.containsKey(id)) {
            return;
        }
        Record record = records.get(id);
        if (record == null) {
            return;
        }
        out.put(id, record);
        collect(record.mother(), depth - 1, out);
        if (record.father() != record.mother()) {
            collect(record.father(), depth - 1, out);
        }
    }
}
