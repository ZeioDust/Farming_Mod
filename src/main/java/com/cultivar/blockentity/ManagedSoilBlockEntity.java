package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.data.NitrogenFixer;
import com.cultivar.data.Nutrient;
import com.cultivar.data.SpeciesDefinition;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Phenotype;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ManagedSoilBlockEntity extends SyncedBlockEntity {
    public static final float PH_MIN = 3.5f;
    public static final float PH_MAX = 9.0f;
    public static final float DEFAULT_LEVEL = 60f;
    public static final float DEFAULT_PH = 6.5f;

    private float nitrogen = DEFAULT_LEVEL;
    private float phosphorus = DEFAULT_LEVEL;
    private float potassium = DEFAULT_LEVEL;
    private float ph = DEFAULT_PH;
    private Identifier lastSpecies;
    private boolean lastWasFixer;

    public ManagedSoilBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MANAGED_SOIL.get(), pos, state);
    }

    protected ManagedSoilBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float get(Nutrient nutrient) {
        return switch (nutrient) {
            case NITROGEN -> nitrogen;
            case PHOSPHORUS -> phosphorus;
            case POTASSIUM -> potassium;
        };
    }

    public void set(Nutrient nutrient, float value) {
        float v = Mth.clamp(value, 0f, 100f);
        switch (nutrient) {
            case NITROGEN -> nitrogen = v;
            case PHOSPHORUS -> phosphorus = v;
            case POTASSIUM -> potassium = v;
        }
    }

    public float ph() {
        return ph;
    }

    public void setPh(float value) {
        ph = Mth.clamp(value, PH_MIN, PH_MAX);
    }

    public int moisture() {
        BlockState state = getBlockState();
        return state.hasProperty(FarmlandBlock.MOISTURE) ? state.getValue(FarmlandBlock.MOISTURE) : 0;
    }

    public float lowestNutrient() {
        return Math.min(nitrogen, Math.min(phosphorus, potassium));
    }

    public boolean isExhausted() {
        return nitrogen <= 0f || phosphorus <= 0f || potassium <= 0f;
    }

    public Identifier lastSpecies() {
        return lastSpecies;
    }

    public boolean lastWasFixer() {
        return lastWasFixer;
    }

    public void recordHarvest(Identifier species, boolean fixer) {
        this.lastSpecies = species;
        this.lastWasFixer = fixer;
        markUpdated();
    }

    public boolean consumeForStage(SpeciesDefinition species, Phenotype phenotype, Optional<NitrogenFixer> fixer, boolean geneticsOn) {
        if (!CultivarConfig.SOIL_CHEMISTRY_ENABLED.get()) {
            return false;
        }
        float draw = geneticsOn ? phenotype.get(Gene.NUTRIENT_DRAW) : 1.0f;
        float base = species.nutrientUse() * draw * CultivarConfig.NUTRIENT_DEPLETION_MULTIPLIER.get().floatValue();
        for (Nutrient nutrient : Nutrient.values()) {
            float amount = nutrient == species.preferredNutrient() ? base * 2f : base;
            if (nutrient == Nutrient.NITROGEN && fixer.isPresent()) {
                float fixed = fixer.get().nitrogenPerStage() * CultivarConfig.ROTATION_BONUS_MULTIPLIER.get().floatValue();
                set(nutrient, get(nutrient) + fixed);
            } else {
                set(nutrient, get(nutrient) - amount);
            }
        }
        markUpdated();
        return isExhausted();
    }

    public void deplete() {
        if (level != null) {
            BlockPos pos = worldPosition;
            Level world = level;
            world.setBlock(pos, ModBlocks.DEPLETED_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
            if (world instanceof ServerLevel serverLevel) {
                Guide.hintNear(serverLevel, pos, 12, Hint.DEPLETED);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("n", nitrogen);
        output.putFloat("p", phosphorus);
        output.putFloat("k", potassium);
        output.putFloat("ph", ph);
        if (lastSpecies != null) {
            output.store("last_species", Identifier.CODEC, lastSpecies);
        }
        output.putBoolean("last_fixer", lastWasFixer);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        nitrogen = input.getFloatOr("n", DEFAULT_LEVEL);
        phosphorus = input.getFloatOr("p", DEFAULT_LEVEL);
        potassium = input.getFloatOr("k", DEFAULT_LEVEL);
        ph = input.getFloatOr("ph", DEFAULT_PH);
        lastSpecies = input.read("last_species", Identifier.CODEC).orElse(null);
        lastWasFixer = input.getBooleanOr("last_fixer", false);
    }
}
