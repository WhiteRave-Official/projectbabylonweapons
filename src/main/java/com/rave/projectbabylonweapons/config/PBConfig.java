package com.rave.projectbabylonweapons.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PBConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_WEAPONS_TOOLS_RECIPES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("recipes");
        ENABLE_VANILLA_WEAPONS_TOOLS_RECIPES = builder
                .comment("Enable vanilla weapon and tool crafting recipes (not armor).")
                .define("enable_vanilla_weapons_tools_recipes", false);
        builder.pop();

        SPEC = builder.build();
    }

    private PBConfig() {
    }
}