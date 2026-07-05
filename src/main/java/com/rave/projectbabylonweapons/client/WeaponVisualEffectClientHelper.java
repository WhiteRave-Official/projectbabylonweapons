package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class WeaponVisualEffectClientHelper {
    private WeaponVisualEffectClientHelper() {
    }

    public static void play(String effectId, int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) {
            return;
        }

        if (WeaponVisualEffectHelper.DRAGON_DESCEND_CAST_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startDragonDescendCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DRAGON_DESCEND_CAST_BURST.equals(effectId)) {
            PhotonWeaponEffectHelper.burstDragonDescendCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DRAGON_DESCEND_CAST_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopDragonDescendCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.GLACIER_CAST_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startGlacierCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.GLACIER_CAST_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopGlacierCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.GLACIER_CONTACT_WAVE.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnGlacierContactWave(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BLESSING_CAST_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startBlessingCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BLESSING_CAST_BURST.equals(effectId)) {
            PhotonWeaponEffectHelper.burstBlessingCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BLESSING_CAST_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopBlessingCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BLESSING_HEAL_PULSE.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnBlessingHealPulse(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BLESSING_ABSORPTION_PULSE.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnBlessingAbsorptionPulse(entity);
            return;
        }
        if (WeaponVisualEffectHelper.FIRE_STORM_CAST_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startFireStormCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.FIRE_STORM_CAST_BURST.equals(effectId)) {
            PhotonWeaponEffectHelper.burstFireStormCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.FIRE_STORM_CAST_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopFireStormCast(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BASTION_FROST_AURA_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startBastionFrostAura(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BASTION_FROST_AURA_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopBastionFrostAura(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BASTION_RULE_AURA_START.equals(effectId)) {
            PhotonWeaponEffectHelper.startBastionRuleAura(entity);
            return;
        }
        if (WeaponVisualEffectHelper.BASTION_RULE_AURA_STOP.equals(effectId)) {
            PhotonWeaponEffectHelper.stopBastionRuleAura(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DIAMOND_SHARD_SPAWN.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnDiamondShardBurst(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DIAMOND_SHARD_DESPAWN.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnDiamondShardBurst(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DRAGON_FURY_CHARGE_SPAWN.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnDragonFuryChargeBurst(entity);
            return;
        }
        if (WeaponVisualEffectHelper.DRAGON_FURY_CHARGE_DESPAWN.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnDragonFuryChargeBurst(entity);
            return;
        }
        if (WeaponVisualEffectHelper.MANA_BUBBLE_CONTACT_BASIC.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnManaBubbleBasicContact(entity);
            return;
        }
        if (WeaponVisualEffectHelper.NETHERITE_BRIMSTONE_BLAST.equals(effectId)) {
            PhotonWeaponEffectHelper.spawnBrimstoneBlast(entity);
        }
    }
}