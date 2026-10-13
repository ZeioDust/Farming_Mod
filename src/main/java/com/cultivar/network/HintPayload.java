package com.cultivar.network;

import com.cultivar.Cultivar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HintPayload(String hint) implements CustomPacketPayload {
    public static final Type<HintPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "hint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HintPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), HintPayload::hint,
            HintPayload::new);

    @Override
    public Type<HintPayload> type() {
        return TYPE;
    }
}
