package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IronBattleWandItemModel extends GeoModel<IronBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(IronBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/iron_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(IronBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/iron_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IronBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/iron_battle_wand.png");
    }
}
