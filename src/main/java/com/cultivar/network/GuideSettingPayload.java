package com.cultivar.network;

import com.cultivar.Cultivar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GuideSettingPayload(boolean hintsOff) implements CustomPacketPayload {
    public static final Type<GuideSettingPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "guide_setting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuideSettingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GuideSettingPayload::hintsOff,
            GuideSettingPayload::new);

    @Override
    public Type<GuideSettingPayload> type() {
        return TYPE;
    }
}
