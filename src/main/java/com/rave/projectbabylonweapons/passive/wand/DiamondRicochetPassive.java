package com.rave.projectbabylonweapons.passive.wand;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DiamondRicochetPassive {
    private static final float RICOCHET_SPEED_RETENTION = 0.96F;
    private static final double RICOCHET_PUSH = 0.08D;
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.wand_diamond.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/diamond_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/diamond_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_diamond.line1").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("diamond")
    );

    private DiamondRicochetPassive() {
    }

    public static void configureProjectile(BasicSpellProjectileEntity projectile) {
        DiamondRicochetBalance.Profile profile = DiamondRicochetBalance.resolve(projectile.getSourceWeapon());
        if (profile == null) {
            return;
        }

        if (projectile.getOwner() instanceof LivingEntity owner && owner.getRandom().nextFloat() < profile.ricochetProcChance()) {
            projectile.setRemainingRicochets(profile.maxRicochets());
        }
    }

    public static boolean tryRicochet(BasicSpellProjectileEntity projectile, BlockHitResult result) {
        if (projectile.getRemainingRicochets() <= 0) {
            return false;
        }

        Vec3 movement = projectile.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            return false;
        }

        Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
        Vec3 reflected = movement.subtract(normal.scale(2.0D * movement.dot(normal)));
        if (reflected.lengthSqr() < 1.0E-6D) {
            reflected = movement.scale(-1.0D);
        }

        projectile.setRemainingRicochets(projectile.getRemainingRicochets() - 1);
        projectile.setPos(
                result.getLocation().x + (normal.x * RICOCHET_PUSH),
                result.getLocation().y + (normal.y * RICOCHET_PUSH),
                result.getLocation().z + (normal.z * RICOCHET_PUSH)
        );
        projectile.setDeltaMovement(reflected.normalize().scale(movement.length() * RICOCHET_SPEED_RETENTION));
        projectile.hasImpulse = true;
        return true;
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }
}



