package com.cultivar;

import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModCreativeTabs;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModMenus;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModRecipes;
import com.cultivar.registry.ModSounds;
import com.cultivar.registry.ModTriggers;
import com.cultivar.registry.ModWorldgen;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Cultivar.MODID)
public class Cultivar {
    public static final String MODID = "cultivar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Cultivar(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModTriggers.TRIGGERS.register(modEventBus);
        ModWorldgen.STRUCTURE_TYPES.register(modEventBus);
        ModWorldgen.STRUCTURE_PIECES.register(modEventBus);
        ModWorldgen.FEATURES.register(modEventBus);
        ModWorldgen.CHUNK_GENERATORS.register(modEventBus);
        ModWorldgen.BIOME_MODIFIERS.register(modEventBus);
        ModWorldgen.LOOT_MODIFIERS.register(modEventBus);
        com.cultivar.gametest.CultivarGameTests.FUNCTIONS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, CultivarConfig.SPEC);
    }
}
