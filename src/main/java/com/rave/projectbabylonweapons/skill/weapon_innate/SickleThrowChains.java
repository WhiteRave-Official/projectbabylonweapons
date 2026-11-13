package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SickleThrowChains {
    private static final Map<UUID, ActiveSickleChains<SickleProjectileEntity>> CHAINS = new HashMap<>();
    private static final Map<UUID, Set<SickleProjectileEntity>> TARGETS = new HashMap<>();

    private SickleThrowChains() { }

    public static int count(ServerPlayer player) {
        var state = CHAINS.get(player.getUUID());
        return state == null ? 0 : state.size();
    }

    public static List<SickleProjectileEntity> projectiles(ServerPlayer player) {
        var state = CHAINS.get(player.getUUID());
        return state == null ? List.of() : state.values();
    }

    public static SickleProjectileEntity anchor(ServerPlayer player) {
        var state = CHAINS.get(player.getUUID());
        return state == null ? null : state.anchor(p -> !p.isRemoved() && p.isTethered());
    }

    public static void add(ServerPlayer player, SickleProjectileEntity projectile) {
        CHAINS.computeIfAbsent(player.getUUID(), id -> new ActiveSickleChains<>()).add(projectile.getId(), projectile);
        SickleThrowSkill.setActiveProjectile(player, projectile.getId());
    }

    public static void bind(SickleProjectileEntity projectile, LivingEntity target) {
        TARGETS.computeIfAbsent(target.getUUID(), id -> new HashSet<>()).add(projectile);
        if (projectile.getOwner() instanceof ServerPlayer player) {
            var state = CHAINS.get(player.getUUID());
            if (state != null) state.select(projectile.getId());
            SickleThrowSkill.setActiveProjectile(player, projectile.getId());
        }
    }

    public static boolean unbind(SickleProjectileEntity projectile, LivingEntity target) {
        var bindings = TARGETS.get(target.getUUID());
        if (bindings == null) return true;
        bindings.remove(projectile);
        if (!bindings.isEmpty()) return false;
        TARGETS.remove(target.getUUID());
        return true;
    }

    public static void removed(ServerPlayer player, SickleProjectileEntity projectile) {
        var state = CHAINS.get(player.getUUID());
        if (state == null) return;
        state.remove(projectile.getId());
        if (state.size() == 0) {
            CHAINS.remove(player.getUUID());
            SickleThrowSkill.clearActiveProjectile(player);
        } else {
            var anchor = anchor(player);
            SickleThrowSkill.setActiveProjectile(player, anchor == null ? state.values().get(0).getId() : anchor.getId());
        }
    }

    public static void discardAll(ServerPlayer player) {
        for (var projectile : projectiles(player)) projectile.discard();
        CHAINS.remove(player.getUUID());
        SickleThrowSkill.clearActiveProjectile(player);
    }

    public static void reset() { CHAINS.clear(); TARGETS.clear(); }
}
