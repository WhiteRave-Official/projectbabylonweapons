package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DiamondBattleWandItemModel extends GeoModel<DiamondBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(DiamondBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/diamond_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DiamondBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/diamond_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiamondBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/diamond_battle_wand.png");
    }
}
