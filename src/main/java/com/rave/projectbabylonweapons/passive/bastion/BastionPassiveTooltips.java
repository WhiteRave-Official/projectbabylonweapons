package com.rave.projectbabylonweapons.passive.bastion;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class BastionPassiveTooltips {
    private static final WeaponPassiveTooltipData DIAMOND = tooltip("diamond", "tooltip.project_babylon_weapons.passive.bastion_diamond.name", "tooltip.project_babylon_weapons.passive.bastion_diamond.line1", "tooltip.project_babylon_weapons.passive.bastion_diamond.line2");
    private static final WeaponPassiveTooltipData GOLDEN = tooltip("gold", "tooltip.project_babylon_weapons.passive.bastion_golden.name", "tooltip.project_babylon_weapons.passive.bastion_golden.line1");
    private static final WeaponPassiveTooltipData ICE = tooltip("ice", "tooltip.project_babylon_weapons.passive.bastion_ice.name", "tooltip.project_babylon_weapons.passive.bastion_ice.line1", "tooltip.project_babylon_weapons.passive.bastion_ice.line2");
    private static final WeaponPassiveTooltipData NETHERITE = tooltip("netherite", "tooltip.project_babylon_weapons.passive.bastion_netherite.name", "tooltip.project_babylon_weapons.passive.bastion_netherite.line1");
    private static final WeaponPassiveTooltipData ETHEREAL = tooltip("ethereal", "tooltip.project_babylon_weapons.passive.bastion_ethereal.name", "tooltip.project_babylon_weapons.passive.bastion_ethereal.line1");
    private static final WeaponPassiveTooltipData DRAGONSTEEL = tooltip("dragonsteel", "tooltip.project_babylon_weapons.passive.bastion_dragonsteel.name", "tooltip.project_babylon_weapons.passive.bastion_dragonsteel.line1", "tooltip.project_babylon_weapons.passive.bastion_dragonsteel.line2");

    private BastionPassiveTooltips() {
    }

    public static WeaponPassiveTooltipData diamond() { return DIAMOND; }
    public static WeaponPassiveTooltipData golden() { return GOLDEN; }
    public static WeaponPassiveTooltipData ice() { return ICE; }
    public static WeaponPassiveTooltipData netherite() { return NETHERITE; }
    public static WeaponPassiveTooltipData ethereal() { return ETHEREAL; }
    public static WeaponPassiveTooltipData dragonsteel() { return DRAGONSTEEL; }

    private static WeaponPassiveTooltipData tooltip(String material, String nameKey, String... lineKeys) {
        return new WeaponPassiveTooltipData(
                Component.translatable(nameKey),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/" + material + "_material_frame.png"),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/" + material + "_material_icon.png"),
                java.util.Arrays.stream(lineKeys).<net.minecraft.network.chat.Component>map(key -> Component.translatable(key).withStyle(ChatFormatting.GRAY)).toList(),
                TooltipFrameStyle.material(material)
        );
    }
}
