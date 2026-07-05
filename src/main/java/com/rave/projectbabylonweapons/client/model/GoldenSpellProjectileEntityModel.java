package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.GoldenSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GoldenSpellProjectileEntityModel extends GeoModel<GoldenSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(GoldenSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/golden_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoldenSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/golden_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GoldenSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/golden_projectile_loop.animation.json");
    }
}