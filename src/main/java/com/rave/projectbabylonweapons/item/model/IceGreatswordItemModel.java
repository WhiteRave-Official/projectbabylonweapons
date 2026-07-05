package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.greatsword.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceGreatswordItemModel extends GeoModel<IceGreatswordItem> {
    @Override
    public ResourceLocation getAnimationResource(IceGreatswordItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/ice_greatsword.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(IceGreatswordItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/ice_greatsword.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceGreatswordItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/ice_greatsword.png");
    }
}
