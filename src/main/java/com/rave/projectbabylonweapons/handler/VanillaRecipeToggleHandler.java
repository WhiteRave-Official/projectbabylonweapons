package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.config.PBConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID)
public final class VanillaRecipeToggleHandler {
    private static final Set<ResourceLocation> DISABLED_RECIPE_IDS = Set.of(
            ResourceLocation.parse("minecraft:diamond_axe"),
            ResourceLocation.parse("minecraft:diamond_hoe"),
            ResourceLocation.parse("minecraft:diamond_pickaxe"),
            ResourceLocation.parse("minecraft:diamond_shovel"),
            ResourceLocation.parse("minecraft:diamond_sword"),
            ResourceLocation.parse("minecraft:golden_axe"),
            ResourceLocation.parse("minecraft:golden_hoe"),
            ResourceLocation.parse("minecraft:golden_pickaxe"),
            ResourceLocation.parse("minecraft:golden_shovel"),
            ResourceLocation.parse("minecraft:golden_sword"),
            ResourceLocation.parse("minecraft:iron_axe"),
            ResourceLocation.parse("minecraft:iron_hoe"),
            ResourceLocation.parse("minecraft:iron_pickaxe"),
            ResourceLocation.parse("minecraft:iron_shovel"),
            ResourceLocation.parse("minecraft:iron_sword"),
            ResourceLocation.parse("minecraft:netherite_axe_smithing"),
            ResourceLocation.parse("minecraft:netherite_hoe_smithing"),
            ResourceLocation.parse("minecraft:netherite_pickaxe_smithing"),
            ResourceLocation.parse("minecraft:netherite_shovel_smithing"),
            ResourceLocation.parse("minecraft:netherite_sword_smithing")
    );

    private VanillaRecipeToggleHandler() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (PBConfig.ENABLE_VANILLA_WEAPONS_TOOLS_RECIPES.get()) {
            return;
        }

        MinecraftServer server = event.getServer();
        RecipeManager recipeManager = server.getRecipeManager();
        List<RecipeHolder<?>> filteredRecipes = recipeManager.getRecipes().stream()
                .filter(recipe -> !DISABLED_RECIPE_IDS.contains(recipe.id()))
                .collect(Collectors.toList());

        int removedCount = recipeManager.getRecipes().size() - filteredRecipes.size();
        if (removedCount > 0) {
            recipeManager.replaceRecipes(filteredRecipes);
            ProjectBabylonWeapons.LOGGER.info("Removed {} vanilla weapon/tool recipes because recipes.enable_vanilla_weapons_tools_recipes=false", removedCount);
        }
    }
}

