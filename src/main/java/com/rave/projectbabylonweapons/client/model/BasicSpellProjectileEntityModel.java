package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BasicSpellProjectileEntityModel extends GeoModel<BasicSpellProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(BasicSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/basic_spell_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BasicSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/basic_spell_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BasicSpellProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/projectile_loop_animation.geo.json");
    }
}
