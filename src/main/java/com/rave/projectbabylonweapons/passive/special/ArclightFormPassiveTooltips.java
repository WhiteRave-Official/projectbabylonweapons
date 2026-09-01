package com.rave.projectbabylonweapons.passive.special;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class ArclightFormPassiveTooltips {
    private static final WeaponPassiveTooltipData CONDUIT = create(
            "arclight",
            "tooltip.project_babylon_weapons.passive.conduit.name",
            "tooltip.project_babylon_weapons.passive.conduit.line1",
            "tooltip.project_babylon_weapons.passive.conduit.line2",
            "tooltip.project_babylon_weapons.passive.conduit.line3",
            "tooltip.project_babylon_weapons.passive.conduit.line4"
    );
    private static final WeaponPassiveTooltipData UNITY = create(
            "evergate",
            "tooltip.project_babylon_weapons.passive.unity.name",
            "tooltip.project_babylon_weapons.passive.unity.line1",
            "tooltip.project_babylon_weapons.passive.unity.line2",
            "tooltip.project_babylon_weapons.passive.unity.line3"
    );

    private ArclightFormPassiveTooltips() {
    }

    public static WeaponPassiveTooltipData conduit() {
        return CONDUIT;
    }

    public static WeaponPassiveTooltipData unity() {
        return UNITY;
    }

    private static WeaponPassiveTooltipData create(String material, String nameKey, String... descriptionKeys) {
        List<Component> lines = java.util.Arrays.stream(descriptionKeys)
                .map(key -> (Component) Component.translatable(key).withStyle(ChatFormatting.GRAY))
                .toList();
        return new WeaponPassiveTooltipData(
                Component.translatable(nameKey),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID,
                        "textures/gui/tooltip/frame/material/" + material + "_material_frame.png"),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID,
                        "textures/gui/tooltip/icon/material/" + material + "_passive_icon.png"),
                lines,
                TooltipFrameStyle.material(material)
        );
    }
}