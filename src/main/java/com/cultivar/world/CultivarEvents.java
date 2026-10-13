package com.cultivar.world;

import com.cultivar.Cultivar;
import com.cultivar.blockentity.HarvestCollectorBlockEntity;
import com.cultivar.command.CultivarCommands;
import com.cultivar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class CultivarEvents {
    private CultivarEvents() {
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("cultivar");
            LevelState.get(level).tick(level);
            profiler.pop();
        }
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("cultivar_async");
        AsyncWork.drain();
        profiler.pop();
        TickStats.endTick();
    }

    @SubscribeEvent
    static void onBlockChanged(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelState.get(level).climate().onBlockChanged(level, event.getPos());
        }
    }

    @SubscribeEvent
    static void onGameEvent(VanillaGameEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && (event.getVanillaEvent().is(GameEvent.BLOCK_OPEN) || event.getVanillaEvent().is(GameEvent.BLOCK_CLOSE))) {
            LevelState.get(level).climate().onBlockChanged(level, BlockPos.containing(event.getEventPosition()));
        }
    }

    @SubscribeEvent
    static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelState.get(level).climate().onChunkLoaded(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelState.clear(level);
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        LevelState.clearAll();
        AsyncWork.shutdown();
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        CultivarCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.HARVEST_COLLECTOR.get(),
                (HarvestCollectorBlockEntity collector, net.minecraft.core.Direction side) -> VanillaContainerWrapper.of(collector));
    }
}
