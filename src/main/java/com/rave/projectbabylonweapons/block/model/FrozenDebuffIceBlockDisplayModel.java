package com.rave.projectbabylonweapons.block.model;

import com.rave.projectbabylonweapons.block.display.FrozenDebuffIceBlockDisplayItem;
import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;


public class FrozenDebuffIceBlockDisplayModel extends GeoModel<FrozenDebuffIceBlockDisplayItem> {
    @Override
    public ResourceLocation getAnimationResource(FrozenDebuffIceBlockDisplayItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/frozen_debuff_ice_block.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(FrozenDebuffIceBlockDisplayItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/frozen_debuff_ice_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrozenDebuffIceBlockDisplayItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/block/frozen_debuff_ice_block.png");
    }
}
