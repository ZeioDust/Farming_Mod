package com.cultivar.world;

import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.Nutrient;
import com.cultivar.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public enum Amendment {
    COMPOST(15f, 15f, 15f, 0f),
    NITROGEN_BLEND(35f, 0f, 0f, 0f),
    PHOSPHORUS_BLEND(0f, 35f, 0f, 0f),
    POTASSIUM_BLEND(0f, 0f, 35f, 0f),
    LIME(0f, 0f, 0f, 0.5f),
    SULFUR(0f, 0f, 0f, -0.5f),
    GREEN_MANURE(20f, 0f, 20f, 0f);

    private final float n;
    private final float p;
    private final float k;
    private final float ph;

    Amendment(float n, float p, float k, float ph) {
        this.n = n;
        this.p = p;
        this.k = k;
        this.ph = ph;
    }

    public void apply(ManagedSoilBlockEntity soil) {
        soil.set(Nutrient.NITROGEN, soil.get(Nutrient.NITROGEN) + n);
        soil.set(Nutrient.PHOSPHORUS, soil.get(Nutrient.PHOSPHORUS) + p);
        soil.set(Nutrient.POTASSIUM, soil.get(Nutrient.POTASSIUM) + k);
        soil.setPh(soil.ph() + ph);
        soil.markUpdated();
    }

    public float need(ManagedSoilBlockEntity soil) {
        return switch (this) {
            case COMPOST -> 100f - soil.lowestNutrient();
            case NITROGEN_BLEND -> 100f - soil.get(Nutrient.NITROGEN);
            case PHOSPHORUS_BLEND -> 100f - soil.get(Nutrient.PHOSPHORUS);
            case POTASSIUM_BLEND -> 100f - soil.get(Nutrient.POTASSIUM);
            case LIME -> Math.max(0f, 6.5f - soil.ph()) * 20f;
            case SULFUR -> Math.max(0f, soil.ph() - 6.5f) * 20f;
            case GREEN_MANURE -> 200f - soil.get(Nutrient.NITROGEN) - soil.get(Nutrient.POTASSIUM);
        };
    }

    public Item item() {
        return switch (this) {
            case COMPOST -> ModItems.COMPOST.get();
            case NITROGEN_BLEND -> ModItems.NUTRIENT_BLEND_N.get();
            case PHOSPHORUS_BLEND -> ModItems.NUTRIENT_BLEND_P.get();
            case POTASSIUM_BLEND -> ModItems.NUTRIENT_BLEND_K.get();
            case LIME -> ModItems.LIME_POWDER.get();
            case SULFUR -> ModItems.SULFUR_POWDER.get();
            case GREEN_MANURE -> ModItems.GREEN_MANURE.get();
        };
    }

    public static @Nullable Amendment of(ItemStack stack) {
        for (Amendment amendment : values()) {
            if (stack.is(amendment.item())) {
                return amendment;
            }
        }
        return null;
    }
}
