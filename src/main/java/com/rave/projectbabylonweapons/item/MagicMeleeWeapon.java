package com.rave.projectbabylonweapons.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

public interface MagicMeleeWeapon {
    ResourceKey<DamageType> getMagicDamageType();

    Attribute getSchoolSpellPowerAttribute();

    float getBaseMagicDamage(ItemStack stack, LivingEntity attacker);

    default float getPhysicalDamageMultiplier() {
        return 0.0F;
    }

    default float getMagicDamageMultiplier() {
        return 1.0F;
    }

    default float getSchoolResistMultiplier(LivingEntity target) {
        return 1.0F;
    }

    default Component getMagicDamageTooltipLabel() {
        return Component.translatable("attribute.project_babylon_weapons.magic_damage");
    }
}
