package com.rave.projectbabylonweapons.item.wand;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.StaffProjectilePatternHelper;
import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.item.renderer.IceBattleWandItemRenderer;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.IceSpellProjectileEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.api.event.types.animation.AttackPhaseEndEvent;

import java.util.function.Consumer;

public class IceBattleWandItem extends SwordItem implements GeoItem, MagicProjectileStaffWeapon {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public static final int DURABILITY = 756;
    public static final int ATTACK_DAMAGE_MOD = 1;
    public static final float ATTACK_SPEED_MOD = -3.0F;
    private static final float BASE_MAGIC_DAMAGE = 2.3F;

    public IceBattleWandItem() {
        super(
                Tiers.DIAMOND,
                new Item.Properties()
                        .durability(2600)
                        .rarity(Rarity.COMMON)
                        .attributes(SwordItem.createAttributes(Tiers.DIAMOND, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD))
        );
    }

    @Override
    public ResourceKey<DamageType> getMagicDamageType() {
        return ISSDamageTypes.ICE_MAGIC;
    }

    @Override
    public Attribute getSchoolSpellPowerAttribute() {
        return AttributeRegistry.ICE_SPELL_POWER.get();
    }

    @Override
    public float getBaseMagicDamage(ItemStack stack, LivingEntity attacker) {
        return BASE_MAGIC_DAMAGE;
    }

    @Override
    public float getSchoolResistMultiplier(LivingEntity target) {
        return Math.max(0.0F, 1.0F + (float) target.getAttributeValue(AttributeRegistry.ICE_MAGIC_RESIST));
    }

    @Override
    public Component getMagicDamageTooltipLabel() {
        return Component.translatable("attribute.project_babylon_weapons.magic_damage");
    }

    @Override
    public BasicSpellProjectileEntity createMagicProjectile(Level level) {
        return new IceSpellProjectileEntity(level);
    }

    @Override
    public float getMagicProjectileSpeed() {
        return 1.44F;
    }

    @Override
    public int getMagicProjectileTrailColor() {
        return 0x87CEEB;
    }


    @Override
    public SoundEvent getMagicProjectileCastSound() {
        return MagicProjectileStaffWeapon.getIronsSpellbooksSound("cast.generic.ice");
    }
    @Override
    public int getMagicProjectileLifetime() {
        return 80;
    }

    @Override
    public boolean shouldFireMagicProjectile(yesman.epicfight.api.animation.AnimationManager.AnimationAccessor<? extends yesman.epicfight.api.animation.types.AttackAnimation> animation,
                                            yesman.epicfight.api.animation.types.AttackAnimation.Phase phase, int phaseOrder) {
        return animation == PBAnimations.WAND_AUTO_1
                || animation == PBAnimations.WAND_AUTO_2
                || animation == PBAnimations.WAND_AUTO_3
                || animation == PBAnimations.WAND_AUTO_4
                || animation == PBAnimations.WAND_DASH
                || animation == PBAnimations.WAND_AIRSlASH;
    }
    @Override
    public void fireMagicProjectiles(ServerPlayerPatch playerPatch, ItemStack weaponStack, AttackPhaseEndEvent event) {
        if (event.getAnimation() == PBAnimations.WAND_AUTO_3) {
            switch (event.getPhaseOrder()) {
                case 0 -> StaffProjectilePatternHelper.fireForwardBackward(playerPatch, weaponStack, this);
                case 1 -> StaffProjectilePatternHelper.firePlus(playerPatch, weaponStack, this);
                case 2 -> StaffProjectilePatternHelper.fireCross(playerPatch, weaponStack, this);
                case 3 -> StaffProjectilePatternHelper.fireForward(playerPatch, weaponStack, this);
                default -> StaffProjectilePatternHelper.fireForward(playerPatch, weaponStack, this);
            }
            return;
        }

        if (event.getAnimation() == PBAnimations.WAND_AUTO_4) {
            switch (event.getPhaseOrder()) {
                case 0 -> StaffProjectilePatternHelper.fireForward(playerPatch, weaponStack, this);
                case 1 -> StaffProjectilePatternHelper.fireAdjacentDoubleForward(playerPatch, weaponStack, this);
                default -> StaffProjectilePatternHelper.fireForward(playerPatch, weaponStack, this);
            }
            return;
        }

        if (event.getAnimation() == PBAnimations.WAND_AIRSlASH) {
            StaffProjectilePatternHelper.fireCircle(playerPatch, weaponStack, this, 6);
            return;
        }

        if (event.getAnimation() == PBAnimations.WAND_DASH) {
            StaffProjectilePatternHelper.fireArrowFormation(playerPatch, weaponStack, this);
            return;
        }

        StaffProjectilePatternHelper.fireForward(playerPatch, weaponStack, this);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new IceBattleWandItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    private PlayState idlePredicate(AnimationState event) {
        if (this.animationprocedure.equals("empty")) {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.ice_wand.idle"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    String prevAnim = "empty";

    private PlayState procedurePredicate(AnimationState event) {
        if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == AnimationController.State.STOPPED || (!this.animationprocedure.equals(prevAnim) && !this.animationprocedure.equals("empty"))) {
            if (!this.animationprocedure.equals(prevAnim))
                event.getController().forceAnimationReset();
            event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.animationprocedure = "empty";
                event.getController().forceAnimationReset();
            }
        } else if (this.animationprocedure.equals("empty")) {
            prevAnim = "empty";
            return PlayState.STOP;
        }
        prevAnim = this.animationprocedure;
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        AnimationController procedureController = new AnimationController(this, "procedureController", 0, this::procedurePredicate);
        data.add(procedureController);
        AnimationController idleController = new AnimationController(this, "idleController", 0, this::idlePredicate);
        data.add(idleController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 2.1F;
    }
}




