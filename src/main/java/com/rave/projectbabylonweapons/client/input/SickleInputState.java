package com.rave.projectbabylonweapons.client.input;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT)
public final class SickleInputState {
    private static int projectile = -1;
    public static final SicklePullInputGate GATE = new SicklePullInputGate();

    private SickleInputState() { }

    public static int projectile() { return projectile; }

    public static void setProjectile(UUID player, int id) {
        var local = Minecraft.getInstance().player;
        if (local != null && local.getUUID().equals(player)) projectile = id;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        projectile = -1;
        GATE.reset();
    }
}
