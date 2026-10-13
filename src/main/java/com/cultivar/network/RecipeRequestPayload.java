package com.cultivar.network;

import com.cultivar.Cultivar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RecipeRequestPayload(Identifier item) implements CustomPacketPayload {
    public static final Type<RecipeRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "recipe_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeRequestPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RecipeRequestPayload::item,
            RecipeRequestPayload::new);

    @Override
    public Type<RecipeRequestPayload> type() {
        return TYPE;
    }
}
