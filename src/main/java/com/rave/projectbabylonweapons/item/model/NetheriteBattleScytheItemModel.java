package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.battlescythe.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetheriteBattleScytheItemModel extends GeoModel<NetheriteBattleScytheItem> {
    @Override
    public ResourceLocation getAnimationResource(NetheriteBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/netherite_battlescythe.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(NetheriteBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/netherite_battlescythe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteBattleScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/netheritescythe.png");
    }
}
