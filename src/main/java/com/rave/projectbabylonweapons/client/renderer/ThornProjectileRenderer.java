package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.client.model.ThornProjectileModel;
import com.rave.projectbabylonweapons.world.entity.projectile.ThornProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ThornProjectileRenderer extends GeoEntityRenderer<ThornProjectileEntity> {
    public ThornProjectileRenderer(EntityRendererProvider.Context context) { super(context, new ThornProjectileModel()); shadowRadius = 0; }
    @Override protected void applyRotations(ThornProjectileEntity entity, PoseStack pose, float age, float yaw, float partialTick) {
        pose.mulPose(Axis.YP.rotationDegrees(180 + Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        pose.translate(.00176 / 16, -5.49355 / 16, .18563 / 16);
    }
}
