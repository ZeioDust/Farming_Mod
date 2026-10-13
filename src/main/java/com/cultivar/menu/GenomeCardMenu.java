package com.cultivar.menu;

import com.cultivar.component.CardData;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class GenomeCardMenu extends AbstractContainerMenu {
    private final CardData card;

    public GenomeCardMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, CardData.STREAM_CODEC.decode(buf));
    }

    public GenomeCardMenu(int id, Inventory inventory, CardData card) {
        super(ModMenus.GENOME_CARD.get(), id);
        this.card = card;
    }

    public CardData card() {
        return card;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().is(ModItems.GENOME_CARD.get()) || player.getOffhandItem().is(ModItems.GENOME_CARD.get());
    }
}
