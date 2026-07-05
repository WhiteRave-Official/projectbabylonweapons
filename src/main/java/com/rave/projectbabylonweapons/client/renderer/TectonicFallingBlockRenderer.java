package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.world.entity.effect.TectonicFallingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TectonicFallingBlockRenderer extends EntityRenderer<TectonicFallingBlockEntity> {
    public TectonicFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TectonicFallingBlockEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        poseStack.pushPose();
        poseStack.translate(0.0D, Mth.lerp(partialTicks, entity.prevAnimY, entity.animY), 0.0D);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(entity.getBlock(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TectonicFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
