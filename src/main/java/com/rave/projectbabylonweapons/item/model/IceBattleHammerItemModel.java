package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.battlehammer.IceBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceBattleHammerItemModel extends GeoModel<IceBattleHammerItem> {
    @Override
    public ResourceLocation getAnimationResource(IceBattleHammerItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/ice_battlehammer.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(IceBattleHammerItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/ice_battlehammer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceBattleHammerItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/icehammer.png");
    }
}
