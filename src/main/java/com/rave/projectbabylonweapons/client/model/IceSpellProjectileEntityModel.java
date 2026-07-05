package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.IceSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceSpellProjectileEntityModel extends GeoModel<IceSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(IceSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/ice_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/ice_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IceSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/ice_spell_projectile_loop.animation.json");
    }
}
