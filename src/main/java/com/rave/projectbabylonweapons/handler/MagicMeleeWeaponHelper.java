package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

public final class MagicMeleeWeaponHelper {
    private static final double MIN_ATTACK_DAMAGE_REFERENCE = 0.001D;

    private MagicMeleeWeaponHelper() {
    }

    public static float calculateRawMagicDamage(LivingEntity attacker, ItemStack weaponStack, MagicMeleeWeapon magicWeapon, float originalDamage) {
        float currentAttackDamage = Math.max((float) MIN_ATTACK_DAMAGE_REFERENCE, (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE));
        float attackContextMultiplier = Math.max(0.0F, originalDamage / currentAttackDamage);
        return calculateRawMagicDamage(attacker, weaponStack, magicWeapon, attackContextMultiplier, magicWeapon.getMagicDamageMultiplier());
    }

    public static float calculateRawMagicDamage(LivingEntity attacker, ItemStack weaponStack, MagicMeleeWeapon magicWeapon, float attackContextMultiplier, float damageMultiplier) {
        float baseMagicDamage = magicWeapon.getBaseMagicDamage(weaponStack, attacker);
        if (baseMagicDamage <= 0.0F || attackContextMultiplier <= 0.0F || damageMultiplier <= 0.0F) {
            return 0.0F;
        }

        double spellPower = resolveSpellPowerMultiplier(AttributeRegistry.SPELL_POWER.get(), attacker.getAttributeValue(AttributeRegistry.SPELL_POWER));
        Attribute schoolAttribute = magicWeapon.getSchoolSpellPowerAttribute();
        double schoolPower = resolveSpellPowerMultiplier(schoolAttribute, attacker.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(schoolAttribute)));

        return (float) (baseMagicDamage * attackContextMultiplier * spellPower * schoolPower * damageMultiplier);
    }

    private static double resolveSpellPowerMultiplier(Attribute attribute, double attributeValue) {
        double clampedValue = Math.max(0.0D, attributeValue);
        if (attribute == null) {
            return clampedValue;
        }

        double defaultValue = attribute.getDefaultValue();
        if (Math.abs(defaultValue) < 0.0001D) {
            return 1.0D + clampedValue;
        }

        return clampedValue;
    }

    public static DamageSource createMagicDamageSource(LivingEntity attacker, ItemStack weaponStack, MagicMeleeWeapon magicWeapon, DamageSource originalSource) {
        Holder<DamageType> damageType = attacker.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(magicWeapon.getMagicDamageType());

        Entity directEntity = originalSource.getDirectEntity() != null ? originalSource.getDirectEntity() : attacker;
        Entity causingEntity = originalSource.getEntity() != null ? originalSource.getEntity() : attacker;

        EpicFightDamageSource magicSource = new EpicFightDamageSource(damageType, directEntity, causingEntity, originalSource.getSourcePosition())
                .setUsedItem(weaponStack);

        if (originalSource instanceof EpicFightDamageSource originalEpicSource) {
            magicSource.setBaseArmorNegation(originalEpicSource.getBaseArmorNegation());
            magicSource.setBaseImpact(originalEpicSource.getBaseImpact());
            magicSource.setStunType(originalEpicSource.getStunType());
            magicSource.setAnimation(originalEpicSource.getAnimation());
            magicSource.setInitialPosition(originalEpicSource.getInitialPosition());
            if (!originalEpicSource.getUsedItem().isEmpty()) {
                magicSource.setUsedItem(originalEpicSource.getUsedItem());
            }
            if (originalEpicSource.is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
                magicSource.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);
            }
        }

        return magicSource;
    }

    public static DamageSource createMagicProjectileDamageSource(LivingEntity attacker, Entity directEntity, ItemStack weaponStack,
                                                                 ResourceKey<DamageType> damageTypeKey, float armorNegation, float impact,
                                                                 StunType stunType) {
        Holder<DamageType> damageType = attacker.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(damageTypeKey);

        EpicFightDamageSource magicSource = new EpicFightDamageSource(damageType, directEntity, attacker, directEntity.position())
                .setUsedItem(weaponStack)
                .setBaseArmorNegation(Math.max(0.0F, armorNegation))
                .setBaseImpact(Math.max(0.0F, impact))
                .setInitialPosition(attacker.position());

        if (stunType != null) {
            magicSource.setStunType(stunType);
        }

        return magicSource;
    }
}
