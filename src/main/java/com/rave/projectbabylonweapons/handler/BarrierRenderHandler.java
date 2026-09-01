package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.BarrierClientState;
import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BarrierRenderHandler {
    private static final float APPEAR_STEP = 0.14F;
    private static final float DISAPPEAR_STEP = 0.10F;
    private static final Map<Integer, VisualState> VISUALS = new HashMap<>();

    private BarrierRenderHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }

        for (Entity rawEntity : level.entitiesForRendering()) {
            if (!(rawEntity instanceof LivingEntity entity) || !entity.isAlive()) {
                continue;
            }

            float amount = BarrierClientState.getAmount(entity.getId());
            VisualState state = VISUALS.get(entity.getId());
            if (amount <= 0.0F && state == null) {
                continue;
            }

            if (state == null) {
                state = new VisualState();
                VISUALS.put(entity.getId(), state);
            }

            if (amount > 0.0F) {
                state.lastAmount = amount;
                state.progress = Math.min(1.0F, state.progress + APPEAR_STEP);
            } else {
                state.progress = Math.max(0.0F, state.progress - DISAPPEAR_STEP);
            }

            if (state.progress > 0.0F) {
                PhotonWeaponEffectHelper.spawnBarrierShield(entity, state.progress, state.tick++, state.lastAmount);
            }
        }

        Iterator<Map.Entry<Integer, VisualState>> iterator = VISUALS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, VisualState> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entry.getValue().progress <= 0.0F || entity == null || !entity.isAlive()) {
                BarrierClientState.update(entry.getKey(), 0.0F);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        VISUALS.clear();
        BarrierClientState.clear();
    }

    private static final class VisualState {
        private float progress;
        private float lastAmount;
        private int tick;
    }
}