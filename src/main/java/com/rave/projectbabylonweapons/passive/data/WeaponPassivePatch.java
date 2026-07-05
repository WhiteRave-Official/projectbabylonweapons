package com.rave.projectbabylonweapons.passive.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record WeaponPassivePatch(ResourceLocation id,
                                 ResourceLocation passiveId,
                                 List<ResourceLocation> weaponIds,
                                 @Nullable TagKey<Item> weaponTag,
                                 JsonObject profile,
                                 @Nullable JsonObject visual) {

    public WeaponPassivePatch {
        weaponIds = List.copyOf(weaponIds);
    }

    public boolean matches(ItemStack stack) {
        return matchesItem(stack) || matchesTag(stack);
    }

    public boolean matchesItem(ItemStack stack) {
        if (this.weaponIds.isEmpty() || stack.isEmpty()) {
            return false;
        }

        ResourceLocation stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.weaponIds.contains(stackItemId);
    }

    public boolean matchesTag(ItemStack stack) {
        return this.weaponTag != null && stack.is(this.weaponTag);
    }

    public boolean isTagPatch() {
        return this.weaponTag != null;
    }

    public static TagKey<Item> createItemTag(ResourceLocation tagId) {
        return ItemTags.create(tagId);
    }
}