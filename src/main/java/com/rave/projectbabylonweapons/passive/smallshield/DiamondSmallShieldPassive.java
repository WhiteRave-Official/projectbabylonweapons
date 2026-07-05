package com.rave.projectbabylonweapons.passive.smallshield;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DiamondSmallShieldPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_diamond.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/diamond_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/diamond_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_diamond.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_diamond.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("diamond")
    );

    private DiamondSmallShieldPassive() {
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}