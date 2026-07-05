package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SickleChainRenderer extends EntityRenderer<SickleProjectileEntity> {
    private static final ResourceLocation CHAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/sickle_chain.png");

    public SickleChainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SickleProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        renderChain(entity, poseStack, buffer, packedLight, partialTicks);

        poseStack.pushPose();


        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ItemStack item = entity.getItem();
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderChain(SickleProjectileEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, float partialTicks) {
        Entity owner = entity.getOwner();
        if (owner == null) {
            return;
        }

        Vec3 start = new Vec3(
                Mth.lerp(partialTicks, owner.xOld, owner.getX()),
                Mth.lerp(partialTicks, owner.yOld, owner.getY()) + owner.getBbHeight() * 0.6,
                Mth.lerp(partialTicks, owner.zOld, owner.getZ())
        );
        Vec3 end = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
        Vec3 diff = end.subtract(start);

        double distance = diff.length();
        if (distance < 0.1) {
            return;
        }

        Vec3 renderOffset = new Vec3(
                Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                Mth.lerp(partialTicks, entity.yOld, entity.getY()),
                Mth.lerp(partialTicks, entity.zOld, entity.getZ())
        );

        Vec3 fromOwner = start.subtract(renderOffset);

        poseStack.pushPose();
        poseStack.translate(fromOwner.x, fromOwner.y, fromOwner.z);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
        renderChainCube(diff, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private static void renderChainCube(Vec3 to, PoseStack poseStack, VertexConsumer buffer, int packedLightIn, int setOverlay) {
        double d = to.horizontalDistance();
        float rotY = (float) (Mth.atan2(to.x, to.z) * (double) (180F / (float) Math.PI));
        float rotX = (float) (-(Mth.atan2(to.y, d) * (double) (180F / (float) Math.PI))) - 90.0F;
        float chainWidth = 3F / 32F;
        float chainOffset = chainWidth * -0.5F;
        float chainLength = (float) to.length() / 2.3F;

        poseStack.pushPose();
        poseStack.scale(2.3F, 2.3F, 2.3F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.translate(0, -chainLength, 0);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        buffer.addVertex(matrix4f, chainOffset, 0, 0)
                .setColor(255, 255, 255, 255)
                .setUv(0f, chainLength)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, chainWidth + chainOffset, 0, 0)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth, chainLength)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, chainWidth + chainOffset, chainLength, 0)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth, 0f)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, chainOffset, chainLength, 0)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 0f)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        float pixelSkip = 4F / 32F;

        buffer.addVertex(matrix4f, 0, pixelSkip, chainOffset)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth, chainLength + pixelSkip)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, 0, pixelSkip, chainWidth + chainOffset)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth * 2, chainLength + pixelSkip)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, 0, chainLength + pixelSkip, chainWidth + chainOffset)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth * 2, pixelSkip)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix4f, 0, chainLength + pixelSkip, chainOffset)
                .setColor(255, 255, 255, 255)
                .setUv(chainWidth, pixelSkip)
                .setOverlay(setOverlay)
                .setLight(packedLightIn)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SickleProjectileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

