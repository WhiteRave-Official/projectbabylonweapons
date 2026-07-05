package com.rave.projectbabylonweapons.client.model;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.effect.GlacierIceSpikeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class GlacierIceSpikeEntityModel extends GeoModel<GlacierIceSpikeEntity> {
    private static final String[] SPIKE_BONES = {"bb_main", "ice1", "ice2", "ice3", "ice4"};
    private static final float BONE_DIAGONAL_ROLL_DEGREES = 35.0F;

    @Override
    public ResourceLocation getModelResource(GlacierIceSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/frozen_debuff_ice_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GlacierIceSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/block/frozen_debuff_ice_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GlacierIceSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/frozen_debuff_ice_block.animation.json");
    }

    @Override
    public void setCustomAnimations(GlacierIceSpikeEntity animatable, long instanceId, AnimationState<GlacierIceSpikeEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        float rollRadians = (animatable.isMirrored() ? BONE_DIAGONAL_ROLL_DEGREES : -BONE_DIAGONAL_ROLL_DEGREES) * Mth.DEG_TO_RAD;
        for (String boneName : SPIKE_BONES) {
            var bone = this.getAnimationProcessor().getBone(boneName);
            if (bone != null) {
                bone.setRotZ(rollRadians);
            }
        }
    }
}
