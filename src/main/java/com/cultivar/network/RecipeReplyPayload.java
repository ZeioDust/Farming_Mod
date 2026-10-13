package com.cultivar.network;

import com.cultivar.Cultivar;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public record RecipeReplyPayload(Identifier item, List<RecipeDisplay> displays) implements CustomPacketPayload {
    public static final Type<RecipeReplyPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Cultivar.MODID, "recipe_reply"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeReplyPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RecipeReplyPayload::item,
            RecipeDisplay.STREAM_CODEC.apply(ByteBufCodecs.list(16)), RecipeReplyPayload::displays,
            RecipeReplyPayload::new);

    @Override
    public Type<RecipeReplyPayload> type() {
        return TYPE;
    }
}
