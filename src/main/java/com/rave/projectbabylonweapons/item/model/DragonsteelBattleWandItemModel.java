package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.wand.DragonsteelBattleWandItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonsteelBattleWandItemModel extends GeoModel<DragonsteelBattleWandItem> {
    @Override
    public ResourceLocation getAnimationResource(DragonsteelBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/dragonsteel_battle_wand_idle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonsteelBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/dragonsteel_battle_wand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonsteelBattleWandItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/dragonsteel_battle_wand.png");
    }
}
