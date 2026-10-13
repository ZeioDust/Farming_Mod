package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.component.CardData;
import com.cultivar.component.PollenLoad;
import com.cultivar.data.GeneticsCodecs;
import com.cultivar.genetics.Genome;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Cultivar.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Genome>> GENOME =
            COMPONENTS.registerComponentType("genome", b -> b.persistent(GeneticsCodecs.GENOME).networkSynchronized(GeneticsCodecs.GENOME_STREAM));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> SPECIES =
            COMPONENTS.registerComponentType("species", b -> b.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> LINEAGE =
            COMPONENTS.registerComponentType("lineage", b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PollenLoad>> POLLEN =
            COMPONENTS.registerComponentType("pollen", b -> b.persistent(PollenLoad.CODEC).networkSynchronized(PollenLoad.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> GRAFT_ROOT =
            COMPONENTS.registerComponentType("graft_root", b -> b.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardData>> CARD =
            COMPONENTS.registerComponentType("card", b -> b.persistent(CardData.CODEC).networkSynchronized(CardData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TRIAL_NUMBER =
            COMPONENTS.registerComponentType("trial_number", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> PEDIGREE_LINE =
            COMPONENTS.registerComponentType("pedigree_line", b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> STRAIN =
            COMPONENTS.registerComponentType("strain", b -> b.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<com.cultivar.menu.SeedBankLine>>> SEED_BANK_LINES =
            COMPONENTS.registerComponentType("seed_bank_lines", b -> b.persistent(com.cultivar.menu.SeedBankLine.CODEC.listOf())
                    .networkSynchronized(com.cultivar.menu.SeedBankLine.STREAM_CODEC.apply(ByteBufCodecs.list(1024))));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.cultivar.market.Grade>> GRADE =
            COMPONENTS.registerComponentType("grade", b -> b.persistent(com.cultivar.market.Grade.CODEC)
                    .networkSynchronized(com.cultivar.market.Grade.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.cultivar.processing.RationData>> RATION =
            COMPONENTS.registerComponentType("ration", b -> b.persistent(com.cultivar.processing.RationData.CODEC)
                    .networkSynchronized(com.cultivar.processing.RationData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.cultivar.mastery.Certificate>> CERTIFICATE =
            COMPONENTS.registerComponentType("certificate", b -> b.persistent(com.cultivar.mastery.Certificate.CODEC)
                    .networkSynchronized(com.cultivar.mastery.Certificate.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NOTEBOOK_PAGE =
            COMPONENTS.registerComponentType("notebook_page", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private ModDataComponents() {
    }
}
