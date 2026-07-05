package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.client.model.ManaBubbleProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.ManaBubbleProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManaBubbleProjectileRenderer extends GeoEntityRenderer<ManaBubbleProjectileEntity> {
    public ManaBubbleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new ManaBubbleProjectileEntityModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ManaBubbleProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getRenderScale();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
