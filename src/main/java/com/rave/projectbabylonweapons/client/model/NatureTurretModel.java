package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.world.entity.mob.NatureTurretEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class NatureTurretModel<T extends NatureTurretEntity> extends GeoModel<T> {
    @Override public ResourceLocation getModelResource(T entity) { return entity.getModelResource(); }
    @Override public ResourceLocation getTextureResource(T entity) { return entity.getTextureResource(); }
    @Override public ResourceLocation getAnimationResource(T entity) { return entity.getAnimationResource(); }
}
