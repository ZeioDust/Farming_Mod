package com.cultivar.entity;

import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.GeneticsCodecs;
import com.cultivar.genetics.Genome;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModSounds;
import com.cultivar.world.PollinationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PollinatorDroneEntity extends MachineEntity {
    public static final double SPEED = 0.22;
    public static final int MAX_AGE = 1200;

    private BlockPos home = BlockPos.ZERO;
    private BlockPos donor = BlockPos.ZERO;
    private BlockPos target = BlockPos.ZERO;
    private int phase;
    private @Nullable Genome pollen;
    private long pollenLineage;
    private @Nullable CropSpecies pollenSpecies;
    private boolean controlled;

    public PollinatorDroneEntity(EntityType<? extends PollinatorDroneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void assign(BlockPos home, BlockPos donor, BlockPos target, boolean controlled) {
        this.controlled = controlled;
        this.home = home.immutable();
        this.donor = donor.immutable();
        this.target = target.immutable();
        this.phase = 0;
        setPos(home.getX() + 0.5, home.getY() + 1.2, home.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            if (isWorking() && random.nextInt(3) == 0) {
                level().addParticle(ModParticles.POLLEN_MOTE.get(), getX(), getY() - 0.1, getZ(), 0, -0.01, 0);
            }
            return;
        }
        if (tickCount > MAX_AGE) {
            finish(level);
            return;
        }
        switch (phase) {
            case 0 -> {
                if (moveToward(Vec3.atCenterOf(donor).add(0, 0.6, 0), SPEED)) {
                    if (level.isLoaded(donor) && level.getBlockEntity(donor) instanceof CropBlockEntity crop && crop.isFlowering() && crop.genome() != null && !crop.isBlighted()) {
                        pollen = crop.genome();
                        pollenLineage = crop.lineage();
                        pollenSpecies = crop.species();
                        setWorking(true);
                        level.sendParticles(ModParticles.POLLEN_MOTE.get(), getX(), getY(), getZ(), 5, 0.2, 0.2, 0.2, 0.01);
                        level.playSound(null, donor, ModSounds.POLLEN_PUFF.get(), SoundSource.BLOCKS, 0.4f, 1.4f);
                        phase = 1;
                    } else {
                        phase = 2;
                    }
                }
            }
            case 1 -> {
                if (moveToward(Vec3.atCenterOf(target).add(0, 0.6, 0), SPEED)) {
                    if (pollen != null && pollenSpecies != null && level.isLoaded(target) && level.getBlockEntity(target) instanceof CropBlockEntity crop
                            && (controlled || !crop.isPollinated()) && level.getRandom().nextFloat() < PollinationSystem.UNIT_CHANCE) {
                        crop.receivePollen(pollenSpecies, pollen, pollenLineage, CropBlockEntity.PollinationMethod.DRONE, null);
                    }
                    setWorking(false);
                    phase = 2;
                }
            }
            default -> {
                if (moveToward(Vec3.atCenterOf(home).add(0, 0.8, 0), SPEED)) {
                    finish(level);
                }
            }
        }
    }

    private void finish(ServerLevel level) {
        if (level.getBlockEntity(home) instanceof PollinationUnitBlockEntity unit) {
            unit.droneReturned();
        }
        discard();
    }

    @Override
    protected void onSwitchedOff(ServerLevel level) {
        if (level.getBlockEntity(home) instanceof PollinationUnitBlockEntity unit) {
            unit.droneReturned();
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        home = input.read("home", BlockPos.CODEC).orElse(BlockPos.ZERO);
        donor = input.read("donor", BlockPos.CODEC).orElse(BlockPos.ZERO);
        target = input.read("target", BlockPos.CODEC).orElse(BlockPos.ZERO);
        phase = input.getIntOr("phase", 2);
        pollen = input.read("pollen", GeneticsCodecs.GENOME).orElse(null);
        pollenLineage = input.getLongOr("pollen_lineage", 0L);
        pollenSpecies = input.getString("pollen_species").map(id -> {
            for (CropSpecies s : CropSpecies.VALUES) {
                if (s.id().equals(id)) {
                    return s;
                }
            }
            return null;
        }).orElse(null);
        controlled = input.getBooleanOr("controlled", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("home", BlockPos.CODEC, home);
        output.store("donor", BlockPos.CODEC, donor);
        output.store("target", BlockPos.CODEC, target);
        output.putInt("phase", phase);
        output.storeNullable("pollen", GeneticsCodecs.GENOME, pollen);
        output.putLong("pollen_lineage", pollenLineage);
        if (pollenSpecies != null) {
            output.putString("pollen_species", pollenSpecies.id());
        }
        output.putBoolean("controlled", controlled);
    }
}
