package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.entity.FallingProduceEntity;
import com.cultivar.entity.HarvesterCartEntity;
import com.cultivar.entity.MistCloudEntity;
import com.cultivar.entity.PollinatorDroneEntity;
import com.cultivar.entity.SoilProbeRoverEntity;
import com.cultivar.entity.SporeDriftEntity;
import com.cultivar.entity.WaterJetEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Cultivar.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<PollinatorDroneEntity>> POLLINATOR_DRONE = ENTITIES.registerEntityType(
            "pollinator_drone", PollinatorDroneEntity::new, MobCategory.MISC,
            b -> b.sized(0.5f, 0.3f).clientTrackingRange(8).updateInterval(2).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<HarvesterCartEntity>> HARVESTER_CART = ENTITIES.registerEntityType(
            "harvester_cart", HarvesterCartEntity::new, MobCategory.MISC,
            b -> b.sized(0.9f, 0.7f).clientTrackingRange(8).updateInterval(2).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<SporeDriftEntity>> SPORE_DRIFT = ENTITIES.registerEntityType(
            "spore_drift", SporeDriftEntity::new, MobCategory.MISC,
            b -> b.sized(0.6f, 0.4f).clientTrackingRange(6).updateInterval(5).noSave().noSummon().noLootTable().fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<WaterJetEntity>> WATER_JET = ENTITIES.registerEntityType(
            "water_jet", WaterJetEntity::new, MobCategory.MISC,
            b -> b.sized(0.25f, 0.25f).clientTrackingRange(6).updateInterval(2).noSave().noSummon().noLootTable().fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<SoilProbeRoverEntity>> SOIL_PROBE_ROVER = ENTITIES.registerEntityType(
            "soil_probe_rover", SoilProbeRoverEntity::new, MobCategory.MISC,
            b -> b.sized(0.6f, 0.3f).clientTrackingRange(8).updateInterval(2).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<FallingProduceEntity>> FALLING_PRODUCE = ENTITIES.registerEntityType(
            "falling_produce", FallingProduceEntity::new, MobCategory.MISC,
            b -> b.sized(0.35f, 0.35f).clientTrackingRange(6).updateInterval(2).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<MistCloudEntity>> MIST_CLOUD = ENTITIES.registerEntityType(
            "mist_cloud", MistCloudEntity::new, MobCategory.MISC,
            b -> b.sized(1.2f, 0.6f).clientTrackingRange(6).updateInterval(5).noSave().noSummon().noLootTable().fireImmune());

    private ModEntities() {
    }
}
