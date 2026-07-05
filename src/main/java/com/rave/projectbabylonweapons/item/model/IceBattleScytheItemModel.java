package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.battlescythe.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceBattleScytheItemModel extends GeoModel<IceBattleScytheItem> {
    @Override
    public ResourceLocation getAnimationResource(IceBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/ice_battlescythe.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(IceBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/ice_battlescythe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/ice_scythe5.png");
    }
}
