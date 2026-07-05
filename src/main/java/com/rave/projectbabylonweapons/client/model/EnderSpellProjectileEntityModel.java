package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.EnderSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EnderSpellProjectileEntityModel extends GeoModel<EnderSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(EnderSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/ender_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EnderSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/ender_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EnderSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/ender_projectile_loop.animation.json");
    }
}