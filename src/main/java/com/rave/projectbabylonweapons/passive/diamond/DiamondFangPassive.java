package com.rave.projectbabylonweapons.passive.diamond;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

import java.util.List;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class DiamondFangPassive {
    private static final float FULL_ARMOR_NEGATION = 100.0F;
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.diamond.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/diamond_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/diamond_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.diamond.line1").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("diamond")
    );

    private DiamondFangPassive() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }

        if (!(event.getSource() instanceof EpicFightDamageSource epicFightDamageSource)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        DiamondFangBalance.Profile profile = DiamondFangBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        if (attacker.getRandom().nextFloat() >= profile.ignoreDefenseProcChance()) {
            return;
        }

        epicFightDamageSource.setBaseArmorNegation(Math.max(epicFightDamageSource.getBaseArmorNegation(), FULL_ARMOR_NEGATION));
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    public static void appendTooltip(List<Component> tooltip) {
        TOOLTIP.appendTooltip(tooltip);
    }
}
