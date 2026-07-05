package com.rave.projectbabylonweapons.item.wand;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.StaffProjectilePatternHelper;
import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.passive.ethereal.EtherealHolyPassive;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.HolySpellProjectileEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.api.event.types.animation.AttackPhaseEndEvent;

import java.util.List;

public class EtherealBattleWandItem extends SwordItem implements MagicProjectileStaffWeapon {
    public static final int DURABILITY = 756;
    public static final int ATTACK_DAMAGE_MOD = 2;
    public static final float ATTACK_SPEED_MOD = -3.0F;
    private static final float BASE_MAGIC_DAMAGE = 3.3F;

    public EtherealBattleWandItem() {
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
        return ISSDamageTypes.HOLY_MAGIC;
    }

    @Override
    public Attribute getSchoolSpellPowerAttribute() {
        return AttributeRegistry.HOLY_SPELL_POWER.get();
    }

    @Override
    public float getBaseMagicDamage(ItemStack stack, LivingEntity attacker) {
        return BASE_MAGIC_DAMAGE;
    }

    @Override
    public float getSchoolResistMultiplier(LivingEntity target) {
        return Math.max(0.0F, 1.0F + (float) target.getAttributeValue(AttributeRegistry.HOLY_MAGIC_RESIST));
    }

    @Override
    public Component getMagicDamageTooltipLabel() {
        return Component.translatable("attribute.project_babylon_weapons.magic_damage");
    }

    @Override
    public BasicSpellProjectileEntity createMagicProjectile(Level level) {
        return new HolySpellProjectileEntity(level);
    }

    @Override
    public float getMagicProjectileSpeed() {
        return 1.44F;
    }

    @Override
    public int getMagicProjectileTrailColor() {
        return 0xF6D77A;
    }


    @Override
    public SoundEvent getMagicProjectileCastSound() {
        return MagicProjectileStaffWeapon.getIronsSpellbooksSound("spell.guiding_bolt.cast");
    }

    @Override
    public float getMagicProjectileCastSoundVolume() {
        return 0.35F;
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

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







