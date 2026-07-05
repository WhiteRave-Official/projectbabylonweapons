package com.rave.projectbabylonweapons.world.capabilities.item;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.Item;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class WeaponCategoryMapper {
    private static final Map<PBWeaponCategories, WeaponCategory> categoryMap = new HashMap();

    public static CapabilityItem.Builder apply(Item item, PBWeaponCategories category) {
        WeaponCategory mappedCategory = (WeaponCategory) categoryMap.getOrDefault(category, category);

        try {
            Method applyMethod = mappedCategory.getClass().getMethod("apply", Item.class);
            return (CapabilityItem.Builder) applyMethod.invoke(mappedCategory, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}