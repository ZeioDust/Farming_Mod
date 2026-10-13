package com.cultivar.network;

import com.cultivar.Cultivar;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MarketQuotesPayload(Map<Identifier, Float> demand) implements CustomPacketPayload {
    public static final Type<MarketQuotesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "market_quotes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarketQuotesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(java.util.HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.FLOAT, 512), MarketQuotesPayload::demand,
            MarketQuotesPayload::new);

    @Override
    public Type<MarketQuotesPayload> type() {
        return TYPE;
    }
}
