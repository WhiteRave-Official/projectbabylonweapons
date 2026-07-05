package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.world.entity.effect.HolyMagicalSealEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HolyMagicalSealEntityModel extends GeoModel<HolyMagicalSealEntity> {
    @Override
    public ResourceLocation getModelResource(HolyMagicalSealEntity animatable) {
        return animatable.getModelResource();
    }

    @Override
    public ResourceLocation getTextureResource(HolyMagicalSealEntity animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public ResourceLocation getAnimationResource(HolyMagicalSealEntity animatable) {
        return animatable.getAnimationResource();
    }
}
