package com.rave.projectbabylonweapons.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class BarrierClientState {
    private static final Map<Integer, Float> AMOUNTS = new ConcurrentHashMap<>();

    private BarrierClientState() {
    }

    public static void update(int entityId, float amount) {
        if (amount > 0.0F) {
            AMOUNTS.put(entityId, amount);
        } else {
            AMOUNTS.remove(entityId);
        }
    }

    public static float getAmount(int entityId) {
        return AMOUNTS.getOrDefault(entityId, 0.0F);
    }

    public static void clear() {
        AMOUNTS.clear();
    }
}