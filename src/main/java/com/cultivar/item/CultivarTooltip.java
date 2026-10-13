package com.cultivar.item;

import com.cultivar.Cultivar;
import com.cultivar.util.Texts;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

public interface CultivarTooltip {
    void addTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> builder);

    final class Keys {
        static BooleanSupplier shift = () -> false;

        private Keys() {
        }

        public static void setShift(BooleanSupplier supplier) {
            shift = supplier;
        }
    }

    static Texts.@org.jspecify.annotations.Nullable Guide guide(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!Cultivar.MODID.equals(id.getNamespace())) {
            return null;
        }
        Texts.Guide entry = Texts.ITEM_GUIDE.get(id.getPath());
        if (entry == null && stack.getItem() instanceof SeedPacketItem) {
            entry = Texts.ITEM_GUIDE.get("seed_packet");
        }
        if (entry == null && stack.getItem() instanceof ProduceItem) {
            entry = Texts.ITEM_GUIDE.get("produce");
        }
        return entry;
    }

    static void appendGuide(ItemStack stack, Consumer<Component> builder) {
        Texts.Guide entry = guide(stack);
        if (entry == null) {
            return;
        }
        if (entry.hasSummary()) {
            builder.accept(Texts.tr(entry.summary()).withStyle(ChatFormatting.GRAY));
        }
        if (entry.details().isEmpty()) {
            return;
        }
        if (Keys.shift.getAsBoolean()) {
            for (String key : entry.details()) {
                builder.accept(Component.literal(" ").append(Texts.tr(key)).withStyle(ChatFormatting.DARK_AQUA));
            }
        } else {
            builder.accept(Texts.tr(Texts.TIP_SHIFT).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @EventBusSubscriber(modid = Cultivar.MODID)
    final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        static void register(RegisterTooltipAppendersEvent event) {
            event.registerAppender(TooltipLocation.POST_CUSTOM, (stack, context, display, player, flag, builder) -> {
                if (stack.getItem() instanceof CultivarTooltip tooltip) {
                    tooltip.addTooltip(stack, context, builder);
                }
                appendGuide(stack, builder);
            });
        }
    }
}
