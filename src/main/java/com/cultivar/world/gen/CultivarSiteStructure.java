package com.cultivar.world.gen;

import com.cultivar.registry.ModWorldgen;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class CultivarSiteStructure extends Structure {
    public static final MapCodec<CultivarSiteStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            SiteKind.CODEC.fieldOf("kind").forGetter(CultivarSiteStructure::kind)
    ).apply(i, CultivarSiteStructure::new));

    private final SiteKind kind;

    public CultivarSiteStructure(StructureSettings settings, SiteKind kind) {
        super(settings);
        this.kind = kind;
    }

    public SiteKind kind() {
        return kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                context.randomState());
        if (y <= context.heightAccessor().getMinY() + 2) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x - kind.width() / 2, y, z - kind.depth() / 2);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new SitePiece(kind, origin))));
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.SITE.get();
    }
}
