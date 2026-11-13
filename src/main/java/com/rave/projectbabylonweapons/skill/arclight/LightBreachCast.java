package com.rave.projectbabylonweapons.skill.arclight;

import com.rave.projectbabylonweapons.world.entity.effect.LightBreachEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class LightBreachCast {
    private static final Map<UUID, LightBreachEntity> PREPARING = new HashMap<>();

    private LightBreachCast() {}

    public static boolean isPreparing(ServerPlayer player) {
        return PREPARING.containsKey(player.getUUID());
    }

    public static boolean begin(ServerPlayer player, LightBreachSettings settings) {
        if (isPreparing(player)) return false;
        LightBreachEntity cast = new LightBreachEntity(player.serverLevel());
        if (!cast.configure(player, settings) || !player.serverLevel().addFreshEntity(cast)) return false;
        PREPARING.put(player.getUUID(), cast);
        LightBreachSounds.seals(player.serverLevel(), cast.position());
        return true;
    }

    public static boolean release(ServerPlayer player) {
        LightBreachEntity cast = PREPARING.remove(player.getUUID());
        if (cast == null || cast.isRemoved() || cast.level() != player.level()) return false;
        cast.activate(player);
        return true;
    }

    public static void cancel(ServerPlayer player) {
        LightBreachEntity cast = PREPARING.remove(player.getUUID());
        if (cast != null) cast.cancelPreparation();
    }

    public static void forget(UUID owner, LightBreachEntity entity) {
        if (owner != null) PREPARING.remove(owner, entity);
    }
}
