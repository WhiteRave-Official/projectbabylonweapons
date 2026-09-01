package com.rave.projectbabylonweapons.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record EvergateNameTooltipData(Component name) implements TooltipComponent {
}