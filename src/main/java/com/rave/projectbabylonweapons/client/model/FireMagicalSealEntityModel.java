package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.world.entity.effect.FireMagicalSealEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FireMagicalSealEntityModel extends GeoModel<FireMagicalSealEntity> {
    @Override
    public ResourceLocation getModelResource(FireMagicalSealEntity animatable) {
        return animatable.getModelResource();
    }

    @Override
    public ResourceLocation getTextureResource(FireMagicalSealEntity animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public ResourceLocation getAnimationResource(FireMagicalSealEntity animatable) {
        return animatable.getAnimationResource();
    }
}
