package com.cultivar.component;

import com.cultivar.genetics.Genome;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record GenomeTooltip(Genome genome) implements TooltipComponent {
}
