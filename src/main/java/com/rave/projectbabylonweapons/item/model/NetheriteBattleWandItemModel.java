package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetheriteBattleWandItemModel extends GeoModel<NetheriteBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(NetheriteBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/netherite_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(NetheriteBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/netherite_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/netherite_battle_wand.png");
    }
}
