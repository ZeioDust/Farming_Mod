package com.cultivar.entity;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.Nutrient;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SoilProbeRoverEntity extends MachineEntity {
    public static final int MAX_STEPS = 16;
    public static final int TICKS_PER_STEP = 10;

    private Direction heading = Direction.NORTH;
    private @Nullable UUID owner;
    private int steps;
    private int readings;
    private float sumN;
    private float sumP;
    private float sumK;
    private float sumPh;
    private @Nullable BlockPos goal;

    public SoilProbeRoverEntity(EntityType<? extends SoilProbeRoverEntity> type, Level level) {
        super(type, level);
    }

    public void deploy(BlockPos start, Direction heading, UUID owner) {
        this.heading = heading.getAxis().isHorizontal() ? heading : Direction.NORTH;
        this.owner = owner;
        setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);
        setYRot(this.heading.toYRot());
    }

    public Direction heading() {
        return heading;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        setWorking(goal == null && tickCount % TICKS_PER_STEP < TICKS_PER_STEP / 2);
        if (goal != null) {
            if (moveToward(Vec3.atBottomCenterOf(goal), 0.12)) {
                setYRot(heading.toYRot());
                sample(level, goal);
                goal = null;
            }
            return;
        }
        if (tickCount % TICKS_PER_STEP != 0) {
            return;
        }
        if (steps >= MAX_STEPS) {
            finish(level);
            return;
        }
        BlockPos here = blockPosition();
        BlockPos next = here.relative(heading);
        if (blocks(level, next)) {
            if (!blocks(level, next.above()) && !blocks(level, here.above())) {
                next = next.above();
            } else {
                finish(level);
                return;
            }
        } else if (!blocks(level, next.below()) && blocks(level, next.below(2))) {
            next = next.below();
        }
        goal = next;
        steps++;
    }

    private static boolean blocks(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !(state.getBlock() instanceof CultivarCropBlock) && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    private void sample(ServerLevel level, BlockPos pos) {
        BlockPos soilPos = pos.below();
        level.sendParticles(ModParticles.SOIL_CRUMB.get(), getX(), getY() + 0.1, getZ(), 4, 0.15, 0.05, 0.15, 0.02);
        level.playSound(null, soilPos, ModSounds.PROBE_CLICK.get(), SoundSource.BLOCKS, 0.4f, 1.5f);
        ServerPlayer player = owner != null ? level.getServer().getPlayerList().getPlayer(owner) : null;
        if (level.getBlockEntity(soilPos) instanceof ManagedSoilBlockEntity soil) {
            readings++;
            sumN += soil.get(Nutrient.NITROGEN);
            sumP += soil.get(Nutrient.PHOSPHORUS);
            sumK += soil.get(Nutrient.POTASSIUM);
            sumPh += soil.ph();
            if (player != null) {
                player.sendSystemMessage(Texts.tr(Texts.MSG_ROVER_READING, steps, Math.round(soil.get(Nutrient.NITROGEN)),
                        Math.round(soil.get(Nutrient.PHOSPHORUS)), Math.round(soil.get(Nutrient.POTASSIUM)),
                        String.format(Locale.ROOT, "%.2f", soil.ph())));
            }
        } else if (player != null) {
            player.sendSystemMessage(Texts.tr(Texts.MSG_ROVER_NO_SOIL, steps));
        }
    }

    @Override
    protected void onSwitchedOff(ServerLevel level) {
        report(level);
    }

    private void finish(ServerLevel level) {
        report(level);
        discard();
    }

    private void report(ServerLevel level) {
        ServerPlayer player = owner != null ? level.getServer().getPlayerList().getPlayer(owner) : null;
        if (player != null) {
            int n = Math.max(1, readings);
            player.sendSystemMessage(Texts.tr(Texts.MSG_ROVER_DONE, readings, Math.round(sumN / n), Math.round(sumP / n), Math.round(sumK / n),
                    String.format(Locale.ROOT, "%.2f", sumPh / n)));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        heading = Direction.from2DDataValue(input.getIntOr("heading", 2));
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        steps = input.getIntOr("steps", 0);
        readings = input.getIntOr("readings", 0);
        sumN = input.getFloatOr("sum_n", 0f);
        sumP = input.getFloatOr("sum_p", 0f);
        sumK = input.getFloatOr("sum_k", 0f);
        sumPh = input.getFloatOr("sum_ph", 0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("heading", heading.get2DDataValue());
        output.storeNullable("owner", UUIDUtil.CODEC, owner);
        output.putInt("steps", steps);
        output.putInt("readings", readings);
        output.putFloat("sum_n", sumN);
        output.putFloat("sum_p", sumP);
        output.putFloat("sum_k", sumK);
        output.putFloat("sum_ph", sumPh);
    }
}
