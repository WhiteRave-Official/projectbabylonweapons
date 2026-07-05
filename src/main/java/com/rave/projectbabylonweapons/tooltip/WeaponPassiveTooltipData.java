package com.rave.projectbabylonweapons.tooltip;

import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record WeaponPassiveTooltipData(Component displayName, ResourceLocation frameTexture,
                                       ResourceLocation iconTexture, List<Component> descriptionLines,
                                       @Nullable TooltipFrameStyle descriptionFrameStyle) {

    public WeaponPassiveTooltipData {
        descriptionLines = List.copyOf(descriptionLines);
    }

    public Component titleLine() {
        return Component.translatable("tooltip.project_babylon_weapons.passive.weapon")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                .append(displayName.copy().withStyle(ChatFormatting.AQUA));
    }

    public Component descriptionHeader() {
        return Component.translatable("tooltip.project_babylon_weapons.passive.description")
                .withStyle(ChatFormatting.GRAY);
    }

    public Component collapsedLine() {
        return Component.translatable("tooltip.project_babylon_weapons.passive.hold_ctrl")
                .withStyle(ChatFormatting.GRAY);
    }

    public void appendTooltip(List<Component> tooltip) {
        tooltip.add(Component.empty());
        if (!Screen.hasControlDown()) {
            tooltip.add(collapsedLine());
            return;
        }

        tooltip.add(titleLine());
        tooltip.add(Component.empty());
        tooltip.add(descriptionHeader());
        tooltip.addAll(descriptionLines);
    }
}