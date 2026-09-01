package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.ArclightMiniProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

public class ArclightMiniProjectileRenderer extends EntityRenderer<ArclightMiniProjectileEntity> {
    public static final ResourceLocation MINI_MODEL =
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "item/arclight_mini_sword");
    public static final ResourceLocation SPEAR_MODEL =
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "item/arclight_spear");

    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float MINI_MODEL_SCALE = 0.8F;
    private static final float SPEAR_MODEL_SCALE = 1.15F;
    private static final float GLOW_SCALE = 1.08F;
    private static final float FORMATION_TICKS = 12.0F;
    private static final float MINI_PORTAL_RETRACT_DISTANCE = 0.9F;
    private static final float SPEAR_PORTAL_RETRACT_DISTANCE = 1.25F;

    public ArclightMiniProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ArclightMiniProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        boolean spear = entity.isSpear();
        BakedModel model = minecraft.getModelManager().getModel(spear ? SPEAR_MODEL : MINI_MODEL);
        ItemStack renderStack = ItemStack.EMPTY;

        float formation = entity.getState() <= ArclightMiniProjectileEntity.STATE_QUEUED
                ? Mth.clamp((entity.tickCount + partialTick) / FORMATION_TICKS, 0.0F, 1.0F)
                : 1.0F;
        Vec3 direction = entity.getFlightDirection();
        float modelScale = spear ? SPEAR_MODEL_SCALE : MINI_MODEL_SCALE;
        float retractDistance = spear ? SPEAR_PORTAL_RETRACT_DISTANCE : MINI_PORTAL_RETRACT_DISTANCE;
        float retract = (1.0F - formation) * retractDistance;

        poseStack.pushPose();
        poseStack.translate(-direction.x * retract, -direction.y * retract, -direction.z * retract);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        float formationScale = modelScale * Mth.lerp(formation, 0.35F, 1.0F);
        renderBaseModel(itemRenderer, model, renderStack, poseStack, bufferSource, formationScale);

        if (formation < 1.0F) {
            float glowAlpha = Mth.sin(formation * Mth.PI);
            renderFormationGlow(model, renderStack, poseStack, bufferSource,
                    formationScale * GLOW_SCALE, glowAlpha);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderBaseModel(ItemRenderer itemRenderer, BakedModel model, ItemStack stack,
                                        PoseStack poseStack, MultiBufferSource bufferSource, float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        for (RenderType renderType : model.getRenderTypes(stack, false)) {
            VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, false);
            itemRenderer.renderModelLists(model, stack, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, consumer);
        }
        poseStack.popPose();
    }

    private static void renderFormationGlow(BakedModel model, ItemStack stack, PoseStack poseStack,
                                             MultiBufferSource bufferSource, float scale, float alpha) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            EntitySnapshot.renderModelLists(
                    pass, stack, FULL_BRIGHT, 0, 1.0F, poseStack,
                    bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageStencil()),
                    Mesh.DrawingFunction.POSITION_TEX
            );
        }
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageStencil());
        }
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            EntitySnapshot.renderModelLists(
                    pass, stack, FULL_BRIGHT, 0, alpha, poseStack,
                    bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageWhite()),
                    Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP
            );
        }
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageWhite());
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ArclightMiniProjectileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}