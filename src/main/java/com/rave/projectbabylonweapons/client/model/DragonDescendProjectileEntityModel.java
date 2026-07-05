package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.world.entity.projectile.DragonDescendProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonDescendProjectileEntityModel extends GeoModel<DragonDescendProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(DragonDescendProjectileEntity animatable) {
        return animatable.getModelResource();
    }

    @Override
    public ResourceLocation getTextureResource(DragonDescendProjectileEntity animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public ResourceLocation getAnimationResource(DragonDescendProjectileEntity animatable) {
        return animatable.getAnimationResource();
    }
}
