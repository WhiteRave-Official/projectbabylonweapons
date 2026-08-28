package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.world.entity.projectile.DragonsteelWyrmEchoProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class DragonsteelWyrmEchoProjectileRenderer extends EntityRenderer<DragonsteelWyrmEchoProjectileEntity> {
    public DragonsteelWyrmEchoProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DragonsteelWyrmEchoProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // Intentionally empty. The projectile is represented only by Photon effects.
    }

    @Override
    public ResourceLocation getTextureLocation(DragonsteelWyrmEchoProjectileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}