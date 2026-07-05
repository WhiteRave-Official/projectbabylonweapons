package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceBattleWandItemModel extends GeoModel<IceBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(IceBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/ice_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(IceBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/ice_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/ice_battle_wand.png");
    }
}
