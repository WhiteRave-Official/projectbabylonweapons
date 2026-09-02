package com.rave.projectbabylonweapons.summon.arclight;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArclightSummonManager {
    private static final Map<UUID, List<UUID>> ACTIVE_WEAPONS = new HashMap<>();
    private static final Map<UUID, ArclightSummonedWeaponType> LAST_SET = new HashMap<>();
    private static final Map<UUID, ArclightSummonedWeaponType> ACTIVE_TYPES = new HashMap<>();

    private ArclightSummonManager() {
    }

    public static void replaceSummon(ServerPlayer owner, ArclightSummonedWeaponEntity.Balance balance) {
        dismiss(owner, true);
        ArclightSummonedWeaponType type = LAST_SET.get(owner.getUUID()) == ArclightSummonedWeaponType.SWORD
                ? ArclightSummonedWeaponType.SPEAR
                : ArclightSummonedWeaponType.SWORD;
        LAST_SET.put(owner.getUUID(), type);

        int count = type == ArclightSummonedWeaponType.SWORD ? 2 : 1;
        List<UUID> ids = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            ArclightSummonedWeaponEntity weapon = new ArclightSummonedWeaponEntity(
                    PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(), owner.level());
            weapon.configure(owner, type, index, balance);
            weapon.setPos(weapon.getOrbitPosition(owner));
            boolean added = owner.level().addFreshEntity(weapon);
            ProjectBabylonWeapons.LOGGER.info("[ArclightSummonDebug] entity_spawn player={} type={} index={} added={} id={}",
                    owner.getName().getString(), type, index, added, weapon.getId());
            ids.add(weapon.getUUID());
        }
        ACTIVE_WEAPONS.put(owner.getUUID(), ids);
        ACTIVE_TYPES.put(owner.getUUID(), type);
    }

    public static void dismiss(ServerPlayer owner, boolean dissolve) {
        dismiss(owner.serverLevel(), owner.getUUID(), dissolve);
    }

    public static void dismiss(ServerLevel level, UUID ownerId, boolean dissolve) {
        if (level == null) {
            ACTIVE_WEAPONS.remove(ownerId);
            ACTIVE_TYPES.remove(ownerId);
            return;
        }
        List<ArclightSummonedWeaponEntity> loadedWeapons = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ArclightSummonedWeaponEntity weapon
                    && ownerId.equals(weapon.getOwnerUuid())) {
                loadedWeapons.add(weapon);
            }
        }
        for (ArclightSummonedWeaponEntity weapon : loadedWeapons) {
            weapon.dismiss(dissolve);
        }
        List<UUID> ids = ACTIVE_WEAPONS.remove(ownerId);
        ACTIVE_TYPES.remove(ownerId);
        if (ids == null) {
            return;
        }
        for (UUID id : ids) {
            Entity entity = level.getEntity(id);
            if (entity instanceof ArclightSummonedWeaponEntity weapon) {
                weapon.dismiss(dissolve);
            }
        }
    }

    public static boolean claim(ArclightSummonedWeaponEntity weapon) {
        UUID ownerId = weapon.getOwnerUuid();
        if (ownerId == null) {
            return false;
        }
        ArclightSummonedWeaponType activeType = ACTIVE_TYPES.putIfAbsent(ownerId, weapon.getWeaponType());
        if (activeType != null && activeType != weapon.getWeaponType()) {
            return false;
        }
        List<UUID> ids = ACTIVE_WEAPONS.computeIfAbsent(ownerId, ignored -> new ArrayList<>());
        if (ids.contains(weapon.getUUID())) {
            return true;
        }
        int limit = weapon.isSpear() ? 1 : 2;
        if (ids.size() >= limit) {
            return false;
        }
        ids.add(weapon.getUUID());
        return true;
    }

    public static void forget(ArclightSummonedWeaponEntity weapon) {
        UUID ownerId = weapon.getOwnerUuid();
        if (ownerId == null) {
            return;
        }
        List<UUID> ids = ACTIVE_WEAPONS.get(ownerId);
        if (ids != null) {
            ids.remove(weapon.getUUID());
            if (ids.isEmpty()) {
                ACTIVE_WEAPONS.remove(ownerId);
                ACTIVE_TYPES.remove(ownerId);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            dismiss(player, false);
            LAST_SET.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            dismiss(player.server.getLevel(event.getFrom()), player.getUUID(), false);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            dismiss(player, true);
        }
    }
}