package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rave.projectbabylonweapons.client.model.GlacierIceSpikeEntityModel;
import com.rave.projectbabylonweapons.world.entity.effect.GlacierIceSpikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GlacierIceSpikeRenderer extends GeoEntityRenderer<GlacierIceSpikeEntity> {
    public GlacierIceSpikeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GlacierIceSpikeEntityModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, GlacierIceSpikeEntity animatable, BakedGeoModel model, @Nullable net.minecraft.client.renderer.RenderType renderType,
                               MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int renderColor) {
        float scale = animatable.getSpikeScale();
        poseStack.scale(scale, scale, scale);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
    }
}
