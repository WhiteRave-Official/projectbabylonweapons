package com.rave.projectbabylonweapons.world.capabilities.item;

import java.util.function.Function;

import net.minecraft.world.item.Item;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public enum PBWeaponCategories implements WeaponCategory, Function<Item, CapabilityItem.Builder> {
    PB_SICKLE,
    ARCLIGHT,
    PB_WAND;

    final int id;

    private PBWeaponCategories() {
        this.id = WeaponCategory.ENUM_MANAGER.assign(this);
    }

    public int universalOrdinal() {
        return this.id;
    }

    public CapabilityItem.Builder apply(Item item) {
        return WeaponCategoryMapper.apply(item, this);
    }
}
