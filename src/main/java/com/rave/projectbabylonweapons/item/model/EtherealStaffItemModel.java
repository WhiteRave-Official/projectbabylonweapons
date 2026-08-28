package com.rave.projectbabylonweapons.item.model;

import com.rave.projectbabylonweapons.item.staff.EtherealStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EtherealStaffItemModel extends GeoModel<EtherealStaffItem> {
    @Override
    public ResourceLocation getAnimationResource(EtherealStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/static_shield.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(EtherealStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "geo/ethereal_staff.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EtherealStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "textures/item/ethereal_staff.png");
    }
}
