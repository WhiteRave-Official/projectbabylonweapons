package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GoldenBattleWandItemModel extends GeoModel<GoldenBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(GoldenBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/golden_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(GoldenBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/golden_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoldenBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/golden_battle_wand.png");
    }
}
