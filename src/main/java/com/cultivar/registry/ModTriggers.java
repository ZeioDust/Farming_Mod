package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.advancement.CultivarTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Cultivar.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, CultivarTrigger> ACTION = TRIGGERS.register("action", CultivarTrigger::new);

    private ModTriggers() {
    }
}
