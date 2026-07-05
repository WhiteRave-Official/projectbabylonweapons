package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.world.entity.effect.DiamondShardEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DiamondShardEntityModel extends GeoModel<DiamondShardEntity> {
    @Override
    public ResourceLocation getModelResource(DiamondShardEntity animatable) {
        if (animatable.getType() == PBModEntities.DIAMOND_SHARD_2.get()) {
            return model("diamond_shard_2.geo.json");
        }
        if (animatable.getType() == PBModEntities.DIAMOND_SHARD_3.get()) {
            return model("diamond_shard_3.geo.json");
        }
        return model("diamond_shard_1.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiamondShardEntity animatable) {
        if (animatable.getType() == PBModEntities.DIAMOND_SHARD_2.get()) {
            return texture("diamond_shard_2.png");
        }
        if (animatable.getType() == PBModEntities.DIAMOND_SHARD_3.get()) {
            return texture("diamond_shard_3.png");
        }
        return texture("diamond_shard_1.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DiamondShardEntity animatable) {
        return null;
    }

    private static ResourceLocation model(String fileName) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/" + fileName);
    }

    private static ResourceLocation texture(String fileName) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/" + fileName);
    }
}