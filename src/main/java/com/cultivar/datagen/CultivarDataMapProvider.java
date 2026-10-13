package com.cultivar.datagen;

import com.cultivar.data.CropSpecies;
import com.cultivar.market.MarketValues;
import com.cultivar.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public class CultivarDataMapProvider extends DataMapProvider {
    public CultivarDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        for (CropSpecies species : CropSpecies.VALUES) {
            compostables.add(ModItems.PRODUCE.get(species), new Compostable(0.65f), false);
            compostables.add(ModItems.SEED_PACKETS.get(species), new Compostable(0.3f), false);
        }
        compostables.add(ModItems.WILD_VINE_BLOCK, new Compostable(0.5f), false);
        compostables.add(ModItems.CANOPY_LEAFMASS, new Compostable(0.5f), false);
        compostables.add(ModItems.GREEN_MANURE, new Compostable(0.85f), false);

        builder(NeoForgeDataMaps.FURNACE_FUELS).add(ModItems.BIOFUEL_CAKE, new FurnaceFuel(2400), false);

        var prices = builder(MarketValues.MARKET_VALUE);
        float[] produce = {0.08f, 0.12f, 0.10f, 0.06f, 0.10f, 0.14f, 0.20f, 0.10f};
        for (CropSpecies species : CropSpecies.VALUES) {
            prices.add(ModItems.PRODUCE.get(species), new MarketValues.Value(produce[species.ordinal()]), false);
        }
        prices.add(ModItems.FIELD_MEAL, new MarketValues.Value(0.25f), false);
        prices.add(ModItems.STARCH, new MarketValues.Value(0.35f), false);
        prices.add(ModItems.SOY_OIL, new MarketValues.Value(0.50f), false);
        prices.add(ModItems.LINEN_CANVAS, new MarketValues.Value(0.50f), false);
        prices.add(ModItems.BIOPLASTIC, new MarketValues.Value(1.00f), false);
        prices.add(ModItems.PICKLED_BRASSICA, new MarketValues.Value(0.40f), false);
        prices.add(ModItems.GOURD_PUREE, new MarketValues.Value(0.14f), false);
        prices.add(ModItems.BIOFUEL_CAKE, new MarketValues.Value(0.35f), false);
        prices.add(ModItems.RATION_PACK, new MarketValues.Value(1.50f), false);
        prices.add(ModItems.RIBBON_BRONZE, new MarketValues.Value(2f), false);
        prices.add(ModItems.RIBBON_SILVER, new MarketValues.Value(4f), false);
        prices.add(ModItems.RIBBON_GOLD, new MarketValues.Value(8f), false);
    }
}
