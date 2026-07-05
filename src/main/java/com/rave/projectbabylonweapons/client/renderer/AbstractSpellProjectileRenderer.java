package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public abstract class AbstractSpellProjectileRenderer<T extends BasicSpellProjectileEntity> extends GeoEntityRenderer<T> {
    protected AbstractSpellProjectileRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F + this.getAdditionalYawRotation(entity)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        // Procedural self-spin: replaces the GeckoLib "idle" loop animation (Epic Fight's GeckoLib 4.8
        // animation-clip lookup does not resolve these entity idle clips at runtime). This reproduces the
        // 1.20.1 visual — the projectile spinning around its own travel axis — without an animation file.
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * this.getSpinDegreesPerTick(entity)));
        float renderScale = entity.getVisualScale();
        poseStack.scale(renderScale, renderScale, renderScale);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    protected float getAdditionalYawRotation(T entity) {
        return 0.0F;
    }

    /** Degrees the projectile self-rotates per tick (visual spin). Override per projectile if needed. */
    protected float getSpinDegreesPerTick(T entity) {
        return 45.0F;
    }
}