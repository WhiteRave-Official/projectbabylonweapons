package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonmaterials.combat.MagicArmorCalculationHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.registry.entries.EpicFightAttributes;

import java.util.Map;

public final class StaffMagicArmorHelper {
    private StaffMagicArmorHelper() {
    }

    public static float resolveMagicArmorNegation(DamageSource source) {
        if (source instanceof EpicFightDamageSource epicFightDamageSource) {
            return Math.max(0.0F, epicFightDamageSource.calculateArmorNegation());
        }
        return 0.0F;
    }

    public static float resolveWeaponMagicArmorNegation(LivingEntity attacker, ItemStack weaponStack) {
        return resolveWeaponDamageAttributeValue(attacker, weaponStack, EpicFightAttributes.ARMOR_NEGATION);
    }

    public static float resolveWeaponImpact(LivingEntity attacker, ItemStack weaponStack) {
        return resolveWeaponDamageAttributeValue(attacker, weaponStack, EpicFightAttributes.IMPACT);
    }

    public static float applyAdjustedMagicDamage(LivingEntity target, float damage, float schoolResistMultiplier, float magicArmorNegationPercent) {
        return MagicArmorCalculationHelper.applyAdjustedMagicDamage(target, damage, schoolResistMultiplier, magicArmorNegationPercent);
    }

    private static float resolveWeaponDamageAttributeValue(LivingEntity attacker, ItemStack weaponStack, Holder<Attribute> attribute) {
        CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapability(weaponStack);
        LivingEntityPatch<?> attackerPatch = EpicFightCapabilities.getEntityPatch(attacker, LivingEntityPatch.class);
        AttributeInstance attributeInstance = attacker.getAttribute(attribute);
        float baseValue = attributeInstance != null ? (float) attributeInstance.getBaseValue() : 0.0F;

        if (itemCapability == null || itemCapability.isEmpty() || attackerPatch == null) {
            return Math.max(0.0F, baseValue);
        }

        Map<Holder<Attribute>, AttributeModifier> damageAttributes = itemCapability.getDamageAttributesInCondition(itemCapability.getStyle(attackerPatch));
        AttributeModifier modifier = damageAttributes.get(attribute);
        if (modifier == null) {
            return Math.max(0.0F, baseValue);
        }

        return Math.max(0.0F, baseValue + (float) modifier.amount());
    }
}
