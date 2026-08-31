package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.world.entity.effect.ArclightRainPortalEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ArclightRainPortalRenderer extends EntityRenderer<ArclightRainPortalEntity> {
    public ArclightRainPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ArclightRainPortalEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
