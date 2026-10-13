package com.cultivar.world.gen;

import com.cultivar.Cultivar;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.LootTable;

public enum SiteKind implements StringRepresentable {
    ABANDONED_TRIAL_FIELD(11, 4, 11),
    RUINED_GREENHOUSE(9, 6, 9),
    OLD_SEED_STORE(7, 6, 7),
    COMPOST_YARD(9, 4, 9),
    BLIGHT_SCAR(11, 3, 11),
    CANOPY_PLATFORM(11, 5, 11),
    WILD_STAND(9, 4, 9),
    CANOPY_BREACH(9, 5, 9);

    public static final Codec<SiteKind> CODEC = StringRepresentable.fromEnum(SiteKind::values);

    private final int width;
    private final int height;
    private final int depth;

    SiteKind(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return id();
    }

    public ResourceKey<LootTable> lootTable() {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(Cultivar.MODID, "chests/" + id()));
    }

    public static SiteKind byId(String id) {
        for (SiteKind kind : values()) {
            if (kind.id().equals(id)) {
                return kind;
            }
        }
        return ABANDONED_TRIAL_FIELD;
    }
}
