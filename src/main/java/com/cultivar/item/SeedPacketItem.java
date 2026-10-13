package com.cultivar.item;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.mastery.Certificate;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.Nullable;

public class SeedPacketItem extends BlockItem implements CultivarTooltip {
    private final CropSpecies species;

    public SeedPacketItem(CropSpecies species, CultivarCropBlock crop, Properties properties) {
        super(crop, properties);
        this.species = species;
    }

    public CropSpecies species() {
        return species;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return Planting.plant(context, species, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        Planting.assignNaturalGenome(stack, species, level);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        appendGenomeTooltip(stack, builder);
    }

    static void appendGenomeTooltip(ItemStack stack, Consumer<Component> builder) {
        Genome genome = stack.get(ModDataComponents.GENOME.get());
        Long lineage = stack.get(ModDataComponents.LINEAGE.get());
        if (genome == null) {
            builder.accept(Texts.tr(Texts.TIP_UNSET_GENOME).withStyle(ChatFormatting.GRAY));
            return;
        }
        if (lineage != null && lineage > 0) {
            builder.accept(Texts.tr(Texts.TIP_LINEAGE, lineage).withStyle(ChatFormatting.GRAY));
        }
        Certificate certificate = stack.get(ModDataComponents.CERTIFICATE.get());
        if (certificate != null) {
            builder.accept(Texts.tr(Texts.TIP_CERTIFIED, certificate.breeder(), certificate.score(), Texts.tr(switch (certificate.ribbon()) {
                case "gold" -> Texts.RIBBON_GOLD;
                case "silver" -> Texts.RIBBON_SILVER;
                case "bronze" -> Texts.RIBBON_BRONZE;
                default -> Texts.RIBBON_NONE;
            })).withStyle(ChatFormatting.GOLD));
            if (certificate.record()) {
                builder.accept(Texts.tr(Texts.TIP_CERTIFIED_RECORD).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        builder.accept(Texts.tr(Texts.TIP_GENOME_UNREAD).withStyle(ChatFormatting.DARK_GRAY));
    }
}
