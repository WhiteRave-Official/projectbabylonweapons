package com.rave.projectbabylonweapons.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class PBKeyMappings {

    public static final KeyMapping PULL_OWNER_TO_TARGET = new KeyMapping(
            "key.projectbabylonweapons.pull_owner_to_target",
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            "key.categories.projectbabylonweapons"
    );

    public static final KeyMapping PULL_TARGET_TO_OWNER = new KeyMapping(
            "key.projectbabylonweapons.pull_target_to_owner",
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            "key.categories.projectbabylonweapons"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(PULL_OWNER_TO_TARGET);
        event.register(PULL_TARGET_TO_OWNER);
    }
}
