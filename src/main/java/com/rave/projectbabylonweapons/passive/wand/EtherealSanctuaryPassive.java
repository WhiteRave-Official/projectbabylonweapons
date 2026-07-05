package com.rave.projectbabylonweapons.passive.wand;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;
import java.util.List;

public final class EtherealSanctuaryPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.wand_ethereal.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/ethereal_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/ethereal_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_ethereal.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_ethereal.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("ethereal")
    );

    private EtherealSanctuaryPassive() {
    }

    public static void onProjectileHit(BasicSpellProjectileEntity projectile, LivingEntity owner) {
        EtherealSanctuaryBalance.Profile profile = EtherealSanctuaryBalance.resolve(projectile.getSourceWeapon());
        if (profile == null || owner.level().isClientSide || owner.getRandom().nextFloat() >= profile.procChance()) {
            return;
        }

        ServerPlayer ally = owner.level().getEntitiesOfClass(ServerPlayer.class,
                        owner.getBoundingBox().inflate(profile.allyRangeBlocks()),
                        player -> player.isAlive() && player != owner && hasNegativeEffect(player))
                .stream()
                .min(Comparator.comparingDouble(owner::distanceToSqr))
                .orElse(null);
        if (ally == null) {
            return;
        }

        MobEffectInstance negativeEffect = ally.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .findFirst()
                .orElse(null);
        if (negativeEffect == null) {
            return;
        }

        ally.removeEffect(negativeEffect.getEffect());
        ally.heal((float) (ally.getMaxHealth() * profile.healPercent()));
    }

    private static boolean hasNegativeEffect(LivingEntity entity) {
        return entity.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}
