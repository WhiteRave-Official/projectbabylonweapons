package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.shield.PBGeoShieldItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PBGeoShieldItemModel extends GeoModel<PBGeoShieldItem> {
    @Override
    public ResourceLocation getAnimationResource(PBGeoShieldItem animatable) {
        return animatable.getGeoAnimationResource();
    }

    @Override
    public ResourceLocation getModelResource(PBGeoShieldItem animatable) {
        return animatable.getGeoModelResource();
    }

    @Override
    public ResourceLocation getTextureResource(PBGeoShieldItem animatable) {
        return animatable.getGeoTextureResource();
    }
}