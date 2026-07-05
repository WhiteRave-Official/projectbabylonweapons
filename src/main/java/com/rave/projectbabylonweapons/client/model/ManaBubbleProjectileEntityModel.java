package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.world.entity.projectile.ManaBubbleProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ManaBubbleProjectileEntityModel extends GeoModel<ManaBubbleProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(ManaBubbleProjectileEntity animatable) {
        return animatable.getVisualPreset().modelResource();
    }

    @Override
    public ResourceLocation getTextureResource(ManaBubbleProjectileEntity animatable) {
        return animatable.getVisualPreset().textureResource();
    }

    @Override
    public ResourceLocation getAnimationResource(ManaBubbleProjectileEntity animatable) {
        return animatable.getVisualPreset().animationResource();
    }
}
