package com.cultivar.network;

import com.cultivar.Cultivar;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GuideStatePayload(List<String> milestones, List<String> hintsSeen, boolean hintsOff) implements CustomPacketPayload {
    public static final Type<GuideStatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "guide_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuideStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64).apply(ByteBufCodecs.list(256)), GuideStatePayload::milestones,
            ByteBufCodecs.stringUtf8(64).apply(ByteBufCodecs.list(256)), GuideStatePayload::hintsSeen,
            ByteBufCodecs.BOOL, GuideStatePayload::hintsOff,
            GuideStatePayload::new);

    @Override
    public Type<GuideStatePayload> type() {
        return TYPE;
    }
}
