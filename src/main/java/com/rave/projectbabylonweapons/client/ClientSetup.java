package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.input.PBKeyMappings;
import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.network.CPPullOwnerToTarget;
import com.rave.projectbabylonweapons.network.CPPullTargetToOwner;
import com.rave.projectbabylonweapons.client.input.SickleInputState;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            PBKeyMappings.register(event);
        }
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        boolean pullOwner = false;
        boolean pullTarget = false;
        while (PBKeyMappings.PULL_OWNER_TO_TARGET.consumeClick()) pullOwner = true;
        while (PBKeyMappings.PULL_TARGET_TO_OWNER.consumeClick()) pullTarget = true;
        int id = SickleInputState.projectile();
        boolean ready = mc.player != null && mc.level != null && mc.screen == null
                && mc.level.getEntity(id) instanceof SickleProjectileEntity projectile
                && !projectile.isRemoved() && projectile.isTethered();
        boolean released = !PBKeyMappings.PULL_OWNER_TO_TARGET.isDown() && !PBKeyMappings.PULL_TARGET_TO_OWNER.isDown();
        if (!SickleInputState.GATE.accept(id, ready, released, pullOwner || pullTarget)) return;
        if (pullOwner) PBNetworkManager.sendToServer(new CPPullOwnerToTarget(id));
        else PBNetworkManager.sendToServer(new CPPullTargetToOwner(id));
    }
}
