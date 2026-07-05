package com.rave.projectbabylonweapons.passive.wand;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class IceFrostTouchPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.wand_ice.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/ice_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/ice_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_ice.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_ice.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("ice")
    );

    private IceFrostTouchPassive() {
    }

    public static void onProjectileHit(BasicSpellProjectileEntity projectile, LivingEntity target, LivingEntity owner) {
        IceFrostTouchBalance.Profile profile = IceFrostTouchBalance.resolve(projectile.getSourceWeapon());
        if (profile == null || owner.level().isClientSide) {
            return;
        }

        applyAreaSplashDamage(projectile, target, owner, profile);
        applyFrostEffects(target, owner, profile);
    }

    private static boolean rollChance(LivingEntity attacker, float chance) {
        return attacker.getRandom().nextFloat() < chance;
    }

    private static void applyAreaSplashDamage(BasicSpellProjectileEntity projectile, LivingEntity primaryTarget, LivingEntity owner,
                                              IceFrostTouchBalance.Profile profile) {
        ItemStack sourceWeapon = projectile.getSourceWeapon();
        float rawAreaDamage = projectile.getRawMagicDamage() * 0.10F;
        if (rawAreaDamage <= 0.0F) {
            return;
        }

        AABB area = primaryTarget.getBoundingBox().inflate(profile.freezeRadiusBlocks(), 1.0D, profile.freezeRadiusBlocks());
        for (LivingEntity victim : owner.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != owner && entity != primaryTarget)) {
            float schoolResistMultiplier = sourceWeapon.getItem() instanceof MagicMeleeWeapon magicWeapon
                    ? magicWeapon.getSchoolResistMultiplier(victim)
                    : 1.0F;
            float adjustedDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(
                    victim,
                    rawAreaDamage,
                    schoolResistMultiplier,
                    projectile.getMagicArmorNegationValue()
            );
            if (adjustedDamage <= 0.0F) {
                continue;
            }

            DamageSource splashSource = MagicMeleeWeaponHelper.createMagicProjectileDamageSource(
                    owner,
                    projectile,
                    sourceWeapon,
                    projectile.getMagicDamageTypeKey(),
                    projectile.getMagicArmorNegationValue(),
                    projectile.getImpactValue(),
                    projectile.getProjectileStunType()
            );
            int originalInvulnerableTime = victim.invulnerableTime;
            victim.invulnerableTime = 0;
            try {
                victim.hurt(splashSource, adjustedDamage);
            } finally {
                victim.invulnerableTime = originalInvulnerableTime;
            }

            applyFrostEffects(victim, owner, profile);
        }
    }

    private static void applyFrostEffects(LivingEntity target, LivingEntity owner, IceFrostTouchBalance.Profile profile) {
        Holder<MobEffect> chilledEffect = MobEffectRegistry.CHILLED;
        MobEffectInstance chilledInstance = target.getEffect(chilledEffect);
        if (chilledInstance == null) {
            if (rollChance(owner, profile.chillIProcChance())) {
                target.addEffect(new MobEffectInstance(chilledEffect, profile.chillIDurationTicks(), IceFrostTouchBalance.CHILL_I_AMPLIFIER));
            }
            return;
        }

        int amplifier = chilledInstance.getAmplifier();
        if (amplifier <= IceFrostTouchBalance.CHILL_I_AMPLIFIER) {
            if (rollChance(owner, profile.chillIIProcChance())) {
                target.addEffect(new MobEffectInstance(chilledEffect, profile.chillIIDurationTicks(), IceFrostTouchBalance.CHILL_II_AMPLIFIER));
            }
            return;
        }

        if (amplifier == IceFrostTouchBalance.CHILL_II_AMPLIFIER) {
            if (rollChance(owner, profile.chillIIIProcChance())) {
                target.addEffect(new MobEffectInstance(chilledEffect, profile.chillIIIDurationTicks(), IceFrostTouchBalance.CHILL_III_AMPLIFIER));
            }
            return;
        }

        if (!rollChance(owner, profile.frozenProcChance())) {
            return;
        }

        target.removeEffect(chilledEffect);
        AABB area = target.getBoundingBox().inflate(profile.freezeRadiusBlocks(), 1.0D, profile.freezeRadiusBlocks());
        for (LivingEntity victim : owner.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != owner)) {
            victim.addEffect(new MobEffectInstance(PBMEffects.FROZEN, profile.frozenDurationTicks()));
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}

