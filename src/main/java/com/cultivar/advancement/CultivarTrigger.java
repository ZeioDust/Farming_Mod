package com.cultivar.advancement;

import com.cultivar.guide.Guide;
import com.cultivar.registry.ModTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class CultivarTrigger extends SimpleCriterionTrigger<CultivarTrigger.Instance> {
    public static final String FIRST_CROSS = "first_cross";
    public static final String READ_THE_CODE = "read_the_code";
    public static final String DOMINANT = "dominant";
    public static final String TRUE_LINE = "true_line";
    public static final String ROTATION = "rotation";
    public static final String RESISTANT = "resistant";
    public static final String MUTANT = "mutant";
    public static final String OUT_OF_SEASON = "out_of_season";
    public static final String WILD_BLOOD = "wild_blood";
    public static final String BANKED = "banked";
    public static final String PRIME_HARVEST = "prime_harvest";
    public static final String FIRST_SALE = "first_sale";
    public static final String FIRST_CONTRACT = "first_contract";
    public static final String RATION = "ration";
    public static final String GENE_EDIT = "gene_edit";
    public static final String SHOW_RECORD = "show_record";
    public static final String GOLD_RIBBON = "gold_ribbon";
    public static final String ATLAS_COMPLETE = "atlas_complete";

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, String action) {
        this.trigger(player, instance -> instance.action().equals(action));
    }

    public static void fire(ServerPlayer player, String action) {
        ModTriggers.ACTION.get().trigger(player, action);
        Guide.milestone(player, action);
    }

    public static Criterion<Instance> criterion(String action) {
        return ModTriggers.ACTION.get().createCriterion(new Instance(Optional.empty(), action));
    }

    public record Instance(Optional<ContextAwarePredicate> player, String action) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.fieldOf("action").forGetter(Instance::action)
        ).apply(i, Instance::new));
    }
}
