package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.DiamondSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DiamondSpellProjectileEntityModel extends GeoModel<DiamondSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(DiamondSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/diamond_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiamondSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/diamond_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DiamondSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/diamond_projectile_loop.animation.json");
    }
}