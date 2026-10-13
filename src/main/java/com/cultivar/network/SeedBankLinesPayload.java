package com.cultivar.network;

import com.cultivar.Cultivar;
import com.cultivar.menu.SeedBankLine;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SeedBankLinesPayload(int containerId, List<SeedBankLine> lines, int capacity) implements CustomPacketPayload {
    public static final Type<SeedBankLinesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "seed_bank_lines"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SeedBankLinesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SeedBankLinesPayload::containerId,
            SeedBankLine.STREAM_CODEC.apply(ByteBufCodecs.list(1024)), SeedBankLinesPayload::lines,
            ByteBufCodecs.VAR_INT, SeedBankLinesPayload::capacity,
            SeedBankLinesPayload::new);

    @Override
    public Type<SeedBankLinesPayload> type() {
        return TYPE;
    }
}
