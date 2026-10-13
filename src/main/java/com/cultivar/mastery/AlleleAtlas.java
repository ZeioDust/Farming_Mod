package com.cultivar.mastery;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.guide.Guide;
import com.cultivar.guide.GuideState;
import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AlleleAtlas {
    public static final String PREFIX = "allele:";
    public static final int[] TIERS = {12, 24, 36};

    private AlleleAtlas() {
    }

    public static boolean knows(ServerPlayer player, String alleleId) {
        return player.getData(ModAttachments.GUIDE).done(PREFIX + alleleId);
    }

    public static int count(GuideState state) {
        int n = 0;
        for (String m : state.milestones()) {
            if (m.startsWith(PREFIX)) {
                n++;
            }
        }
        return n;
    }

    public static void record(ServerPlayer player, Genome genome, AlleleTable table) {
        Set<String> ids = new LinkedHashSet<>();
        for (Gene gene : Gene.VALUES) {
            for (int slot = 0; slot < 2; slot++) {
                int code = genome.allele(gene, slot);
                if (table.has(gene, code)) {
                    ids.add(PREFIX + table.get(gene, code).id());
                }
            }
        }
        GuideState before = player.getData(ModAttachments.GUIDE);
        GuideState state = before;
        for (String id : ids) {
            state = state.withMilestone(id);
        }
        if (state.equals(before)) {
            return;
        }
        player.setData(ModAttachments.GUIDE, state);
        int known = count(state);
        for (int i = 0; i < TIERS.length; i++) {
            reward(player, known >= TIERS[i], "atlas_tier_" + (i + 1), i);
        }
        int total = table.all().size();
        reward(player, known >= total, "atlas_complete", 3);
        Guide.sync(player);
    }

    private static void reward(ServerPlayer player, boolean reached, String milestone, int tier) {
        if (!reached || player.getData(ModAttachments.GUIDE).done(milestone)) {
            return;
        }
        player.setData(ModAttachments.GUIDE, player.getData(ModAttachments.GUIDE).withMilestone(milestone));
        ItemStack[] rewards = switch (tier) {
            case 0 -> new ItemStack[]{new ItemStack(Items.EMERALD, 8), new ItemStack(Items.PAPER, 16)};
            case 1 -> new ItemStack[]{new ItemStack(ModItems.GENE_EDITOR_BLUEPRINT.get()), new ItemStack(Items.EMERALD, 12)};
            case 2 -> new ItemStack[]{new ItemStack(ModItems.EDIT_REAGENT.get(), 2), new ItemStack(Items.EMERALD, 16)};
            default -> new ItemStack[]{new ItemStack(ModItems.EDIT_REAGENT.get(), 4), new ItemStack(ModItems.RIBBON_GOLD.get()),
                    new ItemStack(Items.EMERALD, 32)};
        };
        for (ItemStack stack : rewards) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        player.sendSystemMessage(Texts.tr(Texts.MSG_ATLAS_TIER, count(player.getData(ModAttachments.GUIDE))));
        if (tier == 3) {
            CultivarTrigger.fire(player, CultivarTrigger.ATLAS_COMPLETE);
        }
    }

    public static boolean knows(GuideState state, Allele allele) {
        return state.done(PREFIX + allele.id());
    }
}
