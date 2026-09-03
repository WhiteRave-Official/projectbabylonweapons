package com.rave.projectbabylonweapons.summon.arclight.epicfight;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimationProperties;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;

import java.util.List;

public final class ArclightSummonAnimations {
    private static final Collider SWORD_COLLIDER = new MultiOBBCollider(4, 0.34D, 0.22D, 0.95D, 0.0D, 0.0D, -0.55D);
    private static final Collider SPEAR_COLLIDER = new MultiOBBCollider(6, 0.42D, 0.30D, 1.90D, 0.0D, 0.0D, -1.15D);

    public static AnimationAccessor<StaticAnimation> IDLE;
    private static final AnimationAccessor<AttackAnimation>[] SWORD = new AnimationAccessor[6];
    private static final AnimationAccessor<AttackAnimation>[] SPEAR = new AnimationAccessor[6];

    private ArclightSummonAnimations() {
    }

    public static void register(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(ProjectBabylonWeapons.MODID, ArclightSummonAnimations::build);
    }

    public static void build(AnimationManager.AnimationBuilder builder) {
        IDLE = builder.nextAccessor("arclight_summon/idle",
                accessor -> new StaticAnimation(true, accessor, ArclightSummonEpicFightRegistry.ARMATURE));
        registerAttack(builder, ArclightSummonedWeaponEntity.AttackType.STAB, "stab", 0.24F, 0.92F);
        registerAttack(builder, ArclightSummonedWeaponEntity.AttackType.VERTICAL, "vertical", 0.28F, 0.98F);
        registerAttack(builder, ArclightSummonedWeaponEntity.AttackType.HORIZONTAL, "horizontal", 0.24F, 0.94F);
        registerAttack(builder, ArclightSummonedWeaponEntity.AttackType.DASH, "dash", 0.20F, 0.90F);
        registerAttack(builder, ArclightSummonedWeaponEntity.AttackType.SPIN, "spin", 0.16F, 1.12F);
    }

    private static void registerAttack(AnimationManager.AnimationBuilder builder,
                                       ArclightSummonedWeaponEntity.AttackType type,
                                       String path, float trailStart, float trailEnd) {
        SWORD[type.ordinal()] = attack(builder, path, SWORD_COLLIDER, trailStart, trailEnd, false);
        SPEAR[type.ordinal()] = attack(builder, path, SPEAR_COLLIDER, trailStart, trailEnd, true);
    }

    private static AnimationAccessor<AttackAnimation> attack(
            AnimationManager.AnimationBuilder builder, String path, Collider collider,
            float trailStart, float trailEnd, boolean spear) {
        String asset = "arclight_summon/" + path;
        String registryPath = asset + (spear ? "_spear" : "_sword");
        return builder.nextAccessor(registryPath, accessor -> new AttackAnimation(
                0.1F, 0.0F, 0.60F, 1.00F, 1.15F,
                collider, ArclightSummonEpicFightRegistry.ARMATURE.get().searchJointByName("Weapon"),
                accessor, ArclightSummonEpicFightRegistry.ARMATURE
        ).addProperty(PBAnimationProperties.IGNORE_ENTITY_COLLISION, true)
                .addProperty(ClientAnimationProperties.TRAIL_EFFECT,
                List.of(trail(trailStart, trailEnd, spear)))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                        AnimationEvent.SimpleEvent.create((entityPatch, animation, params) -> {
                            if (entityPatch.getOriginal() instanceof ArclightSummonedWeaponEntity weapon) {
                                weapon.finishAnimatedAttack();
                            }
                        }, AnimationEvent.Side.SERVER)));
    }

    private static TrailInfo trail(float start, float end, boolean spear) {
        return TrailInfo.builder()
                .startPos(new Vec3(spear ? -0.38D : -0.24D, 0.0D, 0.15D))
                .endPos(new Vec3(spear ? 0.38D : 0.24D, 0.0D, spear ? -2.1D : -1.25D))
                .joint("Weapon")
                .time(start, end)
                .fadeTime(0.12F)
                .interpolations(5)
                .lifetime(8)
                .updateInterval(1)
                .blockLight(15)
                .skyLight(15)
                .r(0.82F)
                .g(0.94F)
                .b(1.0F)
                .texture("epicfight:textures/particle/swing_trail.png")
                .type(EpicFightParticles.SWING_TRAIL.get())
                .create();
    }

    public static AnimationAccessor<? extends StaticAnimation> forAttack(
            ArclightSummonedWeaponEntity.AttackType attack, boolean spear) {
        AnimationAccessor<AttackAnimation> selected = (spear ? SPEAR : SWORD)[attack.ordinal()];
        return selected == null ? IDLE : selected;
    }
}
