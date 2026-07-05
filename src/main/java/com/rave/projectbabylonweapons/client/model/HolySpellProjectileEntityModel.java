package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.HolySpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HolySpellProjectileEntityModel extends GeoModel<HolySpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(HolySpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/holy_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HolySpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/holy_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HolySpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/holy_projectile_loop.animation.json");
    }
}
