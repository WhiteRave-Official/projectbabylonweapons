package com.rave.projectbabylonweapons.passive.smallshield;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DragonsteelSmallShieldPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_dragonsteel.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/dragonsteel_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/dragonsteel_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_dragonsteel.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.small_shield_dragonsteel.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("dragonsteel")
    );

    private DragonsteelSmallShieldPassive() {
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}
