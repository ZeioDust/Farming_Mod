package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.menu.ClimateControllerMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.util.Texts;
import com.cultivar.world.AsyncWork;
import com.cultivar.world.ClimateScan;
import com.cultivar.world.Environment;
import com.cultivar.world.LevelState;
import com.cultivar.world.TickStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.cultivar.menu.ComputedData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ClimateControllerBlockEntity extends SyncedBlockEntity implements MenuProvider, TickingMachine {
    public static final int D_SEALED = 0;
    public static final int D_VOLUME_LOW = 1;
    public static final int D_VOLUME_HIGH = 2;
    public static final int D_TEMP = 3;
    public static final int D_SETPOINT = 4;
    public static final int D_HUMIDITY = 5;
    public static final int D_LIGHT = 6;
    public static final int D_INSULATION = 7;
    public static final int D_GROW_LIGHTS = 8;
    public static final int D_HUMIDIFIERS = 9;
    public static final int D_DEHUMIDIFIERS = 10;
    public static final int DATA_COUNT = 11;
    public static final int GROW_LIGHT_FLOOR = 14;

    private boolean sealed;
    private int volume;
    private float insulation;
    private int growLights;
    private int humidifiers;
    private int dehumidifiers;
    private float setpoint = 0.8f;
    private float temperature = Float.NaN;
    private float humidity = 0.5f;
    private boolean dirty = true;
    private boolean scanning;
    private long lastScan = Long.MIN_VALUE;

    private final ContainerData data = new ComputedData(DATA_COUNT, this::dataValue);

    private int dataValue(int index) {
        return switch (index) {
            case D_SEALED -> sealed ? 1 : 0;
            case D_VOLUME_LOW -> volume & 0x7FFF;
            case D_VOLUME_HIGH -> (volume >> 15) & 0x7FFF;
            case D_TEMP -> Math.round(temperature() * 100);
            case D_SETPOINT -> Math.round(setpoint * 100);
            case D_HUMIDITY -> Math.round(humidity * 100);
            case D_LIGHT -> lightFloor();
            case D_INSULATION -> Math.round(insulation * 100);
            case D_GROW_LIGHTS -> growLights;
            case D_HUMIDIFIERS -> humidifiers;
            case D_DEHUMIDIFIERS -> dehumidifiers;
            default -> 0;
        };
    }

    public ClimateControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLIMATE_CONTROLLER.get(), pos, state);
    }

    public boolean isSealed() {
        return sealed && CultivarConfig.GREENHOUSE_ENABLED.get();
    }

    public int volume() {
        return volume;
    }

    public float temperature() {
        return Float.isNaN(temperature) ? setpoint : temperature;
    }

    public float setpoint() {
        return setpoint;
    }

    public float humidity() {
        return humidity;
    }

    public float insulation() {
        return insulation;
    }

    public int lightFloor() {
        return growLights > 0 ? GROW_LIGHT_FLOOR : 0;
    }

    public int growLights() {
        return growLights;
    }

    public int humidifiers() {
        return humidifiers;
    }

    public int dehumidifiers() {
        return dehumidifiers;
    }

    public void markDirty() {
        dirty = true;
    }

    public void adjustSetpoint(float delta) {
        setpoint = Mth.clamp(setpoint + delta, -0.5f, 2.0f);
        markUpdated();
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!CultivarConfig.GREENHOUSE_ENABLED.get()) {
            return;
        }
        if (scanning && level.getGameTime() - lastScan > 200) {
            scanning = false;
            dirty = true;
        }
        if (dirty && !scanning && (lastScan == Long.MIN_VALUE || level.getGameTime() - lastScan >= 10)) {
            startScan(level);
        }
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        float natural = Environment.naturalTemperature(level, pos, level.getBiome(pos).value());
        if (Float.isNaN(temperature)) {
            temperature = natural;
        }
        if (sealed) {
            float rate = 0.02f * insulation * CultivarConfig.GREENHOUSE_INSULATION_FACTOR.get().floatValue();
            temperature += (setpoint - temperature) * Math.min(1f, rate);
            float humidityTarget = Mth.clamp(0.5f + 0.25f * humidifiers - 0.25f * dehumidifiers, 0f, 1f);
            humidity += (humidityTarget - humidity) * 0.1f;
        } else {
            temperature += (natural - temperature) * 0.2f;
            humidity += (0.5f - humidity) * 0.2f;
        }
        if (level.getGameTime() % 100 == 0) {
            markUpdated();
        }
    }

    private void startScan(ServerLevel level) {
        dirty = false;
        scanning = true;
        lastScan = level.getGameTime();
        long start = System.nanoTime();
        int max = CultivarConfig.CLIMATE_VOLUME_MAX.get();
        ClimateScan.Snapshot snapshot = ClimateScan.snapshot(level, worldPosition, max);
        TickStats.record(TickStats.CLIMATE, System.nanoTime() - start, 1);
        BlockPos pos = worldPosition.immutable();
        AsyncWork.submit("climate-flood-fill", () -> ClimateScan.floodFill(snapshot), result -> {
            if (isRemoved() || level.getBlockEntity(pos) != this) {
                return;
            }
            scanning = false;
            applyScan(level, result);
        });
    }

    private void applyScan(ServerLevel level, ClimateScan.Result result) {
        sealed = result.sealed();
        volume = result.volume().size();
        insulation = result.insulation();
        growLights = result.growLights();
        humidifiers = result.humidifiers();
        dehumidifiers = result.dehumidifiers();
        if (sealed) {
            LevelState.get(level).climate().put(worldPosition, result.volume(), result.boundary(), true);
        } else {
            it.unimi.dsi.fastutil.longs.LongOpenHashSet watch = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(result.volume());
            watch.addAll(result.boundary());
            for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-2, -2, -2), worldPosition.offset(2, 2, 2))) {
                watch.add(p.asLong());
            }
            LevelState.get(level).climate().put(worldPosition, new it.unimi.dsi.fastutil.longs.LongOpenHashSet(), watch, false);
            volume = 0;
        }
        markUpdated();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            LevelState.get(serverLevel).climate().remove(worldPosition);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        dirty = true;
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_CLIMATE);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ClimateControllerMenu(id, inventory, data, this, worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("setpoint", setpoint);
        output.putFloat("temperature", Float.isNaN(temperature) ? setpoint : temperature);
        output.putFloat("humidity", humidity);
        output.putBoolean("sealed", sealed);
        output.putInt("volume", volume);
        output.putFloat("insulation", insulation);
        output.putInt("grow_lights", growLights);
        output.putInt("humidifiers", humidifiers);
        output.putInt("dehumidifiers", dehumidifiers);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setpoint = input.getFloatOr("setpoint", 0.8f);
        temperature = input.getFloatOr("temperature", setpoint);
        humidity = input.getFloatOr("humidity", 0.5f);
        sealed = input.getBooleanOr("sealed", false);
        volume = input.getIntOr("volume", 0);
        insulation = input.getFloatOr("insulation", 0f);
        growLights = input.getIntOr("grow_lights", 0);
        humidifiers = input.getIntOr("humidifiers", 0);
        dehumidifiers = input.getIntOr("dehumidifiers", 0);
    }
}
