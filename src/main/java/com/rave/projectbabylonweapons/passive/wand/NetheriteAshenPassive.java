package com.rave.projectbabylonweapons.passive.wand;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class NetheriteAshenPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.wand_netherite.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/netherite_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/netherite_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_netherite.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_netherite.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("netherite")
    );

    private NetheriteAshenPassive() {
    }

    public static void onProjectileHit(BasicSpellProjectileEntity projectile, LivingEntity primaryTarget, LivingEntity owner, DamageSource damageSource) {
        NetheriteAshenBalance.Profile profile = NetheriteAshenBalance.resolve(projectile.getSourceWeapon());
        if (profile == null || owner.level().isClientSide) {
            return;
        }

        ItemStack sourceWeapon = projectile.getSourceWeapon();
        float rawAreaDamage = projectile.getRawMagicDamage() * profile.aoeDamageMultiplier();
        if (rawAreaDamage <= 0.0F) {
            return;
        }

        AABB area = primaryTarget.getBoundingBox().inflate(profile.radiusBlocks(), 1.0D, profile.radiusBlocks());
        for (LivingEntity victim : owner.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != owner)) {
            float schoolResistMultiplier = sourceWeapon.getItem() instanceof MagicMeleeWeapon magicWeapon
                    ? magicWeapon.getSchoolResistMultiplier(victim)
                    : 1.0F;
            float adjustedDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(victim, rawAreaDamage, schoolResistMultiplier, projectile.getMagicArmorNegationValue());
            if (adjustedDamage <= 0.0F) {
                continue;
            }

            DamageSource aoeSource = MagicMeleeWeaponHelper.createMagicProjectileDamageSource(
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
                victim.hurt(aoeSource, adjustedDamage);
            } finally {
                victim.invulnerableTime = originalInvulnerableTime;
            }

            if (owner.getRandom().nextFloat() < profile.igniteProcChance()) {
                victim.igniteForSeconds(profile.igniteSeconds());
            }
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}
