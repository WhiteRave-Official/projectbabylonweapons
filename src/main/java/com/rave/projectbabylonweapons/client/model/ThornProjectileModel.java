package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.projectile.ThornProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ThornProjectileModel extends GeoModel<ThornProjectileEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/thorn_projectile.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/thorn_projectile.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/thorn_projectile.animation.json");
    @Override public ResourceLocation getModelResource(ThornProjectileEntity entity) { return MODEL; }
    @Override public ResourceLocation getTextureResource(ThornProjectileEntity entity) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(ThornProjectileEntity entity) { return ANIMATIONS; }
}
