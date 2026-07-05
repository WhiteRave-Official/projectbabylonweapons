package com.rave.projectbabylonweapons.item.material;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum PBToolTiers implements Tier {
    GOLDEN_DURABLE(
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            Tiers.DIAMOND.getUses(),
            Tiers.DIAMOND.getSpeed(),
            Tiers.DIAMOND.getAttackDamageBonus(),
            Tiers.GOLD.getEnchantmentValue(),
            () -> ingredientOrFallback(
                    ResourceLocation.fromNamespaceAndPath("project_babylon_materials", "infused_gold_ingot"),
                    Items.GOLD_INGOT
            )
    ),
    ICE(
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            1800,
            8.5F,
            3.5F,
            14,
            () -> ingredientOrFallback(
                    ResourceLocation.fromNamespaceAndPath("project_babylon_materials", "everfrost_ingot"),
                    Items.DIAMOND
            )
    );

    private final TagKey<Block> incorrectBlocksForDrops;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    PBToolTiers(
            TagKey<Block> incorrectBlocksForDrops,
            int uses,
            float speed,
            float attackDamageBonus,
            int enchantmentValue,
            Supplier<Ingredient> repairIngredient
    ) {
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    private static Ingredient ingredientOrFallback(ResourceLocation id, Item fallback) {
        Item resolved = BuiltInRegistries.ITEM.get(id);
        if (resolved == null || resolved == Items.AIR) {
            return Ingredient.of(fallback);
        }
        return Ingredient.of(resolved);
    }
}
