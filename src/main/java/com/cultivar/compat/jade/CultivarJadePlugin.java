package com.cultivar.compat.jade;

import com.cultivar.Cultivar;
import java.util.Locale;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

@WailaPlugin
public class CultivarJadePlugin implements IWailaPlugin {
    static final Identifier UID = Identifier.fromNamespaceAndPath(Cultivar.MODID, "block_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(Server.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Client.INSTANCE, Block.class);
    }

    private static boolean ours(BlockAccessor accessor) {
        return accessor.getBlockEntity() != null
                && Cultivar.MODID.equals(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(accessor.getBlock()).getNamespace());
    }

    enum Server implements StreamServerDataProvider<BlockAccessor, BlockInfo> {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return UID;
        }

        @Override
        public boolean shouldRequestData(BlockAccessor accessor) {
            return ours(accessor);
        }

        @Override
        public @Nullable BlockInfo streamData(BlockAccessor accessor) {
            BlockEntity entity = accessor.getBlockEntity();
            return entity != null && ours(accessor) ? BlockInfo.of(entity, accessor.getBlockState(), accessor.getPlayer()) : null;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlockInfo> streamCodec() {
            return BlockInfo.STREAM_CODEC;
        }
    }

    enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!ours(accessor)) {
                return;
            }
            Server.INSTANCE.decodeFromData(accessor).ifPresent(info -> {
                for (Component line : info.lines()) {
                    tooltip.add(line);
                }
                if (info.progress() >= 0) {
                    ProgressView view = ProgressView.read(new ProgressView.Data(Math.max(0f, Math.min(1f, info.progress()))));
                    view.text = Component.literal(String.format(Locale.ROOT, "%d%%", Math.round(info.progress() * 100)));
                    tooltip.add(JadeUI.progress(view));
                }
            });
        }
    }
}
