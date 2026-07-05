package com.rave.projectbabylonweapons.gameasset;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Random;

public class PBReusableEvents {
    private static final Vec3f GROUNDSLAM_OFFSET = new Vec3f(0.0F, 0.0F, -1.4F);

    public static final AnimationEvent.E0 BIG_GROUNDSLAM = (entitypatch, self, params) ->
            groundSlam(entitypatch, self, GROUNDSLAM_OFFSET, 50.0, 3.5F, 0.0F, 1.1F);

    public static final AnimationEvent.E0 MEDIUM_GROUNDSLAM = (entitypatch, self, params) ->
            groundSlam(entitypatch, self, GROUNDSLAM_OFFSET, 42.0, 2.75F, 0.5F, 1.3F);

    public static final AnimationEvent.E0 SMALL_GROUNDSLAM = (entitypatch, self, params) ->
            groundSlam(entitypatch, self, GROUNDSLAM_OFFSET, 35.0, 2.0F, 1.0F, 1.5F);

    private static void groundSlam(
            LivingEntityPatch<?> entitypatch,
            AssetAccessor<? extends StaticAnimation> self,
            Vec3f offset,
            double particleStrength,
            float fractureRadius,
            float poseTime,
            float pitchBase
    ) {
        Vec3 position = ((LivingEntity) entitypatch.getOriginal()).position();
        OpenMatrix4f modelTransform = entitypatch.getArmature()
                .getBoundTransformFor(entitypatch.getAnimator().getPose(poseTime), ((HumanoidArmature) Armatures.BIPED.get()).toolR)
                .mulFront(OpenMatrix4f.createTranslation((float) position.x, (float) position.y, (float) position.z)
                        .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS).mulBack(entitypatch.getModelMatrix(1.0F))));

        Vec3 weaponEdge = OpenMatrix4f.transform(modelTransform, offset.toDoubleVector());
        Level level = ((LivingEntity) entitypatch.getOriginal()).level();
        Vec3 floorPos = getFloor(entitypatch, self.get(), offset, ((HumanoidArmature) Armatures.BIPED.get()).toolR);
        BlockState blockState = level.getBlockState(new BlockPos.MutableBlockPos(floorPos.x, floorPos.y, floorPos.z));

        if (entitypatch instanceof PlayerPatch) {
            level.playSound(
                    (Player) entitypatch.getOriginal(),
                    entitypatch.getOriginal(),
                    blockState.is(Blocks.WATER) ? SoundEvents.GENERIC_SPLASH : (SoundEvent) EpicFightSounds.SLAM_HEAVY.get(),
                    SoundSource.PLAYERS,
                    1.5F,
                    pitchBase - (new Random().nextFloat() - 0.5F) * 0.2F
            );
        }

        weaponEdge = new Vec3(weaponEdge.x, floorPos.y, weaponEdge.z);
        level.addParticle(
                (ParticleOptions) EpicFightParticles.GROUND_SLAM.get(),
                floorPos.x,
                (double) ((int) floorPos.y + 1),
                floorPos.z,
                particleStrength / 50.0,
                particleStrength,
                particleStrength / 50.0
        );

        LevelUtil.circleSlamFracture((LivingEntity) entitypatch.getOriginal(), level, weaponEdge, fractureRadius, true, true);
    }

    private static Vec3 getFloor(LivingEntityPatch<?> entitypatch, StaticAnimation self, Vec3f weaponOffset, Joint joint) {
        float dpx = weaponOffset.x + (float) ((LivingEntity) entitypatch.getOriginal()).getX();
        float dpy = weaponOffset.y + (float) ((LivingEntity) entitypatch.getOriginal()).getY();
        float dpz = weaponOffset.z + (float) ((LivingEntity) entitypatch.getOriginal()).getZ();

        if (joint != null) {
            OpenMatrix4f transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(1.0F), joint);
            transformMatrix.translate(weaponOffset);
            OpenMatrix4f correction = new OpenMatrix4f().rotate(-Math.toRadians(((LivingEntity) entitypatch.getOriginal()).yRotO + 180.0F), new Vec3f(0.0F, 1.0F, 0.0F));
            OpenMatrix4f.mul(correction, transformMatrix, transformMatrix);
            dpx = transformMatrix.m30 + (float) ((LivingEntity) entitypatch.getOriginal()).getX();
            dpy = transformMatrix.m31 + (float) ((LivingEntity) entitypatch.getOriginal()).getY();
            dpz = transformMatrix.m32 + (float) ((LivingEntity) entitypatch.getOriginal()).getZ();
        }

        BlockState block = ((LivingEntity) entitypatch.getOriginal()).level().getBlockState(new BlockPos.MutableBlockPos(dpx, dpy, dpz));
        dpy = new BlockPos.MutableBlockPos(dpx, dpy, dpz).getY();

        while ((block.getBlock() instanceof BushBlock || block.isAir()) && !block.is(Blocks.VOID_AIR) && dpy > -63.0F) {
            block = ((LivingEntity) entitypatch.getOriginal()).level().getBlockState(new BlockPos.MutableBlockPos(dpx, dpy -= 1.0F, dpz));
        }

        return new Vec3(dpx, dpy, dpz);
    }
}
