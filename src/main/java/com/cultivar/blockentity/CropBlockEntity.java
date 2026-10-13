package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.GeneticsCodecs;
import com.cultivar.data.NitrogenFixer;
import com.cultivar.data.PhBand;
import com.cultivar.data.SpeciesDefinition;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModParticles;
import com.cultivar.util.Rng;
import com.cultivar.world.Environment;
import com.cultivar.world.GrowthRules;
import com.cultivar.world.LevelState;
import com.cultivar.world.PedigreeData;
import com.cultivar.world.PollinationSystem;
import com.cultivar.world.dimension.CultivarDimensions;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class CropBlockEntity extends SyncedBlockEntity {
    public enum PollinationMethod implements StringRepresentable {
        NONE, WIND, BRUSH, DRONE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private @Nullable CropSpecies species;
    private @Nullable Genome genome;
    private long lineage;
    private boolean wild;
    private @Nullable Genome donor;
    private long donorLineage;
    private PollinationMethod method = PollinationMethod.NONE;
    private @Nullable UUID crossedBy;
    private float progress;
    private int stress;
    private @Nullable Identifier strain;
    private int blightTicks;
    private @Nullable Genome rootGenome;
    private boolean rescuedByGreenhouse;
    private int lifeUpdates;
    private float lifeEfficiency;
    private GrowthRules.Limit lastLimit = GrowthRules.Limit.NONE;

    private transient @Nullable Phenotype phenotype;
    private transient float neighbourhoodSimilarity = -1f;
    private transient long similarityComputedAt = Long.MIN_VALUE;

    public CropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CROP.get(), pos, state);
        if (state.getBlock() instanceof CultivarCropBlock crop && crop.species() != null) {
            this.species = crop.species();
        }
    }

    public @Nullable CropSpecies species() {
        return species;
    }

    public Identifier speciesKey() {
        return species != null ? species.key() : CropSpecies.WHEATLINE.key();
    }

    public @Nullable Genome genome() {
        return genome;
    }

    public long lineage() {
        return lineage;
    }

    public boolean isWild() {
        return wild;
    }

    public boolean isGrafted() {
        return rootGenome != null;
    }

    public @Nullable Genome rootGenome() {
        return rootGenome;
    }

    public void setSpecies(CropSpecies species) {
        this.species = species;
        setChanged();
    }

    public void plant(Genome genome, long lineage, boolean wild) {
        resetGrowingRecord();
        this.genome = genome;
        this.lineage = lineage;
        this.wild = wild;
        this.phenotype = null;
        refreshVisuals();
        markUpdated();
    }

    public void makeGraft(CropSpecies species, Genome scion, long scionLineage, Genome root) {
        resetGrowingRecord();
        this.species = species;
        this.genome = scion;
        this.lineage = scionLineage;
        this.rootGenome = root;
        this.donor = null;
        this.method = PollinationMethod.NONE;
        this.phenotype = null;
        refreshVisuals();
        markUpdated();
    }

    public void ensureGenome(ServerLevel level) {
        if (genome != null) {
            return;
        }
        BlockState current = getBlockState();
        if (strain == null && current.hasProperty(CultivarCropBlock.BLIGHTED) && current.getValue(CultivarCropBlock.BLIGHTED)) {
            java.util.List<Identifier> strains = Catalog.get(level).strainsFor(speciesKey());
            if (!strains.isEmpty()) {
                strain = strains.get(level.getRandom().nextInt(strains.size()));
                LevelState.get(level).markInfected(worldPosition, true);
            }
        }
        Catalog catalog = Catalog.get(level);
        wild = level.dimension().equals(CultivarDimensions.CANOPY_LEVEL);
        genome = wild ? catalog.wildGenome(speciesKey(), Rng.of(level.getRandom())) : catalog.naturalGenome(speciesKey(), Rng.of(level.getRandom()));
        lineage = PedigreeData.get(level.getServer()).create(speciesKey(), 0, 0,
                wild ? PedigreeData.Kind.WILD : PedigreeData.Kind.NATURAL, level.getGameTime(), genome.toHex());
        phenotype = null;
        refreshVisuals();
        markUpdated();
    }

    public Phenotype phenotype() {
        if (!CultivarConfig.GENETICS_ENABLED.get()) {
            return Phenotype.neutral();
        }
        Phenotype p = phenotype;
        if (p == null) {
            if (genome == null) {
                return Phenotype.neutral();
            }
            Catalog catalog = level != null ? Catalog.get(level) : Catalog.defaults();
            Phenotype scion = Phenotype.resolve(genome, catalog.table());
            p = rootGenome != null ? Phenotype.graft(Phenotype.resolve(rootGenome, catalog.table()), scion) : scion;
            phenotype = p;
        }
        return p;
    }

    public void refreshVisuals() {
        phenotype = null;
        if (level == null || level.isClientSide() || isRemoved() || level.getBlockEntity(worldPosition) != this) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof CultivarCropBlock)) {
            return;
        }
        Phenotype p = phenotype();
        int vigor = CultivarCropBlock.vigorFor(p.get(Gene.GROWTH_RATE));
        int density = CultivarCropBlock.densityFor(p.get(Gene.YIELD));
        BlockState updated = state.setValue(CultivarCropBlock.VIGOR, vigor).setValue(CultivarCropBlock.DENSITY, density)
                .setValue(CultivarCropBlock.BLIGHTED, strain != null);
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
    }

    public boolean isFlowering() {
        return CultivarCropBlock.stage(getBlockState()) == CultivarCropBlock.FLOWERING;
    }

    public boolean isBagged() {
        BlockState state = getBlockState();
        return state.hasProperty(CultivarCropBlock.BAGGED) && state.getValue(CultivarCropBlock.BAGGED);
    }

    public boolean isPollinated() {
        return donor != null;
    }

    public @Nullable Genome donor() {
        return donor;
    }

    public long donorLineage() {
        return donorLineage;
    }

    public PollinationMethod pollinationMethod() {
        return method;
    }

    public @Nullable UUID crossedBy() {
        return crossedBy;
    }

    public boolean receivePollen(CropSpecies pollenSpecies, Genome pollen, long pollenLineage, PollinationMethod how, @Nullable UUID by) {
        if (!isFlowering() || isBagged() || isGrafted() || genome == null || pollenSpecies != species) {
            return false;
        }
        if (donor != null && how == PollinationMethod.WIND) {
            return false;
        }
        donor = pollen;
        donorLineage = pollenLineage;
        method = how;
        crossedBy = by;
        markUpdated();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.POLLEN_MOTE.get(), worldPosition.getX() + 0.5, worldPosition.getY() + 0.7,
                    worldPosition.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.01);
        }
        return true;
    }

    public boolean isBlighted() {
        return strain != null;
    }

    public @Nullable Identifier strain() {
        return strain;
    }

    public int blightTicks() {
        return blightTicks;
    }

    public void infect(Identifier strainId) {
        if (strain != null || level == null) {
            return;
        }
        strain = strainId;
        blightTicks = 0;
        if (level instanceof ServerLevel serverLevel) {
            LevelState.get(serverLevel).markInfected(worldPosition, true);
            Guide.hintNear(serverLevel, worldPosition, 16, Hint.BLIGHT);
        }
        refreshVisuals();
        markUpdated();
    }

    public boolean advanceBlight(int lethalTicks) {
        blightTicks++;
        setChanged();
        return blightTicks >= lethalTicks;
    }

    public float neighbourhoodSimilarity() {
        return neighbourhoodSimilarity;
    }

    public long similarityComputedAt() {
        return similarityComputedAt;
    }

    public void setNeighbourhoodSimilarity(float similarity, long gameTime) {
        this.neighbourhoodSimilarity = similarity;
        this.similarityComputedAt = gameTime;
    }

    public float progress() {
        return progress;
    }

    public float growingEfficiency() {
        return lifeUpdates == 0 ? 1f : lifeEfficiency / lifeUpdates;
    }

    private void resetGrowingRecord() {
        lifeUpdates = 0;
        lifeEfficiency = 0f;
    }

    public int stress() {
        return stress;
    }

    public GrowthRules.Limit lastLimit() {
        return lastLimit;
    }

    public boolean rescuedByGreenhouse() {
        return rescuedByGreenhouse;
    }

    public void addProgress(float amount) {
        progress += amount;
        setChanged();
    }

    public void growthUpdate(ServerLevel level, int interval) {
        ensureGenome(level);
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof CultivarCropBlock)) {
            return;
        }
        if (strain != null) {
            return;
        }
        int stage = CultivarCropBlock.stage(state);
        Catalog catalog = Catalog.get(level);
        SpeciesDefinition def = catalog.species(speciesKey());
        PhBand band = catalog.phBand(speciesKey());
        boolean genetics = CultivarConfig.GENETICS_ENABLED.get();
        Phenotype p = phenotype();
        Environment env = Environment.sample(level, worldPosition);
        GrowthRules.Verdict verdict = GrowthRules.evaluate(p, def, band, env, genetics);
        lastLimit = verdict.limit();

        if (verdict.lethal()) {
            stress++;
            if (stress >= GrowthRules.STRESS_TO_DIE) {
                die(level, false);
                return;
            }
        } else if (stress > 0) {
            stress--;
        }

        if (stage < CultivarCropBlock.MAX_STAGE) {
            lifeUpdates++;
            lifeEfficiency += verdict.canGrow() ? Math.min(1f, verdict.speed()) : 0f;
        }
        if (stage < CultivarCropBlock.MAX_STAGE && verdict.canGrow()) {
            if (env.greenhouse() != null && !rescuedByGreenhouse) {
                Environment natural = Environment.sample(level, worldPosition, false);
                if (!GrowthRules.evaluate(p, def, band, natural, genetics).canGrow()) {
                    rescuedByGreenhouse = true;
                }
            }
            float rate = (genetics ? p.get(Gene.GROWTH_RATE) : 1f) * CultivarConfig.GROWTH_RATE_MULTIPLIER.get().floatValue();
            progress += interval * rate * verdict.speed();
            int ticksPerStage = Math.max(20, def.ticksPerStage());
            if (progress >= ticksPerStage) {
                progress -= ticksPerStage;
                advanceStage(level, state, stage + 1, def, catalog.nitrogenFixer(speciesKey()), p, genetics);
                state = getBlockState();
                stage = CultivarCropBlock.stage(state);
            }
            setChanged();
        }

        if (stage == CultivarCropBlock.FLOWERING && donor == null && !isBagged() && !isGrafted()) {
            PollinationSystem.tryWindPollination(level, this);
        }
    }

    private void advanceStage(ServerLevel level, BlockState state, int newStage, SpeciesDefinition def,
                              Optional<NitrogenFixer> fixer, Phenotype p, boolean genetics) {
        level.setBlock(worldPosition, state.setValue(CultivarCropBlock.STAGE, newStage), Block.UPDATE_CLIENTS);
        if (newStage == CultivarCropBlock.FLOWERING) {
            Guide.hintNear(level, worldPosition, 12, Hint.FLOWERING);
        }
        if (level.getBlockEntity(worldPosition.below()) instanceof ManagedSoilBlockEntity soil) {
            if (soil.consumeForStage(def, p, fixer, genetics)) {
                soil.deplete();
            }
        }
        if (newStage == CultivarCropBlock.MAX_STAGE && level.getBlockEntity(worldPosition.below()) instanceof ManagedSoilBlockEntity soil) {
            soil.recordHarvest(speciesKey(), fixer.isPresent());
        }
    }

    public void boostGrowth(ServerLevel level, float amount) {
        ensureGenome(level);
        BlockState state = getBlockState();
        if (strain != null || !(state.getBlock() instanceof CultivarCropBlock)) {
            return;
        }
        int stage = CultivarCropBlock.stage(state);
        if (stage >= CultivarCropBlock.MAX_STAGE) {
            return;
        }
        Catalog catalog = Catalog.get(level);
        SpeciesDefinition def = catalog.species(speciesKey());
        progress += amount;
        int ticksPerStage = Math.max(20, def.ticksPerStage());
        if (progress >= ticksPerStage) {
            progress -= ticksPerStage;
            advanceStage(level, state, stage + 1, def, catalog.nitrogenFixer(speciesKey()), phenotype(), CultivarConfig.GENETICS_ENABLED.get());
        }
        setChanged();
    }

    public void regrowAfterHarvest() {
        resetGrowingRecord();
        progress = 0;
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(CultivarCropBlock.STAGE, CultivarCropBlock.FLOWERING + 1), Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    public void die(ServerLevel level, boolean fromBlight) {
        BlockPos pos = worldPosition;
        BlockState below = level.getBlockState(pos.below());
        level.sendParticles(ModParticles.SPORE_SPECK.get(), pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (fromBlight && (below.getBlock() instanceof com.cultivar.block.ManagedSoilBlock
                || below.getBlock() instanceof net.minecraft.world.level.block.FarmlandBlock)) {
            level.setBlock(pos.below(), com.cultivar.registry.ModBlocks.DEPLETED_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            LevelState state = LevelState.get(serverLevel);
            state.registerCrop(worldPosition);
            if (strain != null) {
                state.markInfected(worldPosition, true);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            LevelState state = LevelState.get(serverLevel);
            state.unregisterCrop(worldPosition);
            state.markInfected(worldPosition, false);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        phenotype = null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (species != null) {
            output.putString("species", species.id());
        }
        if (genome != null) {
            output.store("genome", GeneticsCodecs.GENOME, genome);
        }
        output.putLong("lineage", lineage);
        output.putBoolean("wild", wild);
        if (donor != null) {
            output.store("donor", GeneticsCodecs.GENOME, donor);
            output.putLong("donor_lineage", donorLineage);
        }
        output.putString("method", method.name());
        if (crossedBy != null) {
            output.store("crossed_by", UUIDUtil.CODEC, crossedBy);
        }
        output.putFloat("progress", progress);
        output.putInt("stress", stress);
        if (strain != null) {
            output.store("strain", Identifier.CODEC, strain);
            output.putInt("blight_ticks", blightTicks);
        }
        if (rootGenome != null) {
            output.store("root", GeneticsCodecs.GENOME, rootGenome);
        }
        output.putBoolean("rescued", rescuedByGreenhouse);
        output.putInt("life_updates", lifeUpdates);
        output.putFloat("life_efficiency", lifeEfficiency);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("species").ifPresent(id -> {
            for (CropSpecies s : CropSpecies.VALUES) {
                if (s.id().equals(id)) {
                    species = s;
                }
            }
        });
        genome = input.read("genome", GeneticsCodecs.GENOME).orElse(null);
        lineage = input.getLongOr("lineage", 0L);
        wild = input.getBooleanOr("wild", false);
        donor = input.read("donor", GeneticsCodecs.GENOME).orElse(null);
        donorLineage = input.getLongOr("donor_lineage", 0L);
        try {
            method = PollinationMethod.valueOf(input.getStringOr("method", "NONE"));
        } catch (IllegalArgumentException e) {
            method = PollinationMethod.NONE;
        }
        crossedBy = input.read("crossed_by", UUIDUtil.CODEC).orElse(null);
        progress = input.getFloatOr("progress", 0f);
        stress = input.getIntOr("stress", 0);
        strain = input.read("strain", Identifier.CODEC).orElse(null);
        blightTicks = input.getIntOr("blight_ticks", 0);
        rootGenome = input.read("root", GeneticsCodecs.GENOME).orElse(null);
        rescuedByGreenhouse = input.getBooleanOr("rescued", false);
        lifeUpdates = input.getIntOr("life_updates", 0);
        lifeEfficiency = input.getFloatOr("life_efficiency", 0f);
        phenotype = null;
    }
}
