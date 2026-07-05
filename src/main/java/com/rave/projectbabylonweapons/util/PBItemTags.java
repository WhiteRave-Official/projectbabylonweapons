package com.rave.projectbabylonweapons.util;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class PBItemTags {

    public static final TagKey<Item> NETHERITE_TOOLS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "netherite_tools"));

    public static final TagKey<Item> ICE_TOOLS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "ice_tools"));

    public static final TagKey<Item> GOLDEN_TOOLS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "golden_tools"));

    public static final TagKey<Item> DIAMOND_TOOLS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "diamond_tools"));

    private PBItemTags() {
    }
}
