package com.rave.projectbabylonweapons.block.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import com.rave.projectbabylonweapons.block.entity.FrozenDebuffIceBlockTileEntity;

public class FrozenDebuffIceBlockModel extends GeoModel<FrozenDebuffIceBlockTileEntity> {
    @Override
    public ResourceLocation getAnimationResource(FrozenDebuffIceBlockTileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/frozen_debuff_ice_block.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(FrozenDebuffIceBlockTileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/frozen_debuff_ice_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrozenDebuffIceBlockTileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/block/frozen_debuff_ice_block.png");
    }
}
