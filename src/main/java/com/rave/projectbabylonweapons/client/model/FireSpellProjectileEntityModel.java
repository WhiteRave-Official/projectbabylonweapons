package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.FireSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FireSpellProjectileEntityModel extends GeoModel<FireSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(FireSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/fire_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FireSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/fire_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FireSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/fire_projectile_loop.animation.json");
    }
}
