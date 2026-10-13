package com.cultivar.item;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.InventoryBlockEntity;
import com.cultivar.blockentity.TickingMachine;
import com.cultivar.util.Texts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SoyOilItem extends Item {
    public SoyOilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof InventoryBlockEntity machine)
                || !(machine instanceof TickingMachine)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel() instanceof ServerLevel level) {
            machine.addBoost(CultivarConfig.SOY_OIL_BOOST_TICKS.get());
            level.playSound(null, context.getClickedPos(), SoundEvents.HONEY_DRINK.value(), SoundSource.BLOCKS, 0.7f, 1.3f);
            level.sendParticles(ParticleTypes.WAX_ON, context.getClickedPos().getX() + 0.5, context.getClickedPos().getY() + 0.9,
                    context.getClickedPos().getZ() + 0.5, 8, 0.3, 0.2, 0.3, 0.02);
            if (context.getPlayer() != null) {
                context.getPlayer().sendOverlayMessage(Texts.tr(Texts.MSG_OILED, machine.boostTicks() / 20));
            }
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }
}
