package com.cultivar.compat.jei;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record MachineRecipe(List<List<ItemStack>> inputs, List<List<ItemStack>> outputs, List<Component> notes) {
    public static MachineRecipe of(List<List<ItemStack>> inputs, List<ItemStack> output, Component... notes) {
        return new MachineRecipe(inputs, List.of(output), List.of(notes));
    }
}
