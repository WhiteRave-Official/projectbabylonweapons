package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.network.SPPlayWeaponVisualEffect;
import net.minecraft.world.entity.Entity;

public final class WeaponVisualEffectHelper {
    public static final String DRAGON_DESCEND_CAST_START = "dragon_descend_cast_start";
    public static final String DRAGON_DESCEND_CAST_BURST = "dragon_descend_cast_burst";
    public static final String DRAGON_DESCEND_CAST_STOP = "dragon_descend_cast_stop";
    public static final String GLACIER_CAST_START = "glacier_cast_start";
    public static final String GLACIER_CAST_STOP = "glacier_cast_stop";
    public static final String GLACIER_CONTACT_WAVE = "glacier_contact_wave";
    public static final String BLESSING_CAST_START = "blessing_cast_start";
    public static final String BLESSING_CAST_BURST = "blessing_cast_burst";
    public static final String BLESSING_CAST_STOP = "blessing_cast_stop";
    public static final String BLESSING_HEAL_PULSE = "blessing_heal_pulse";
    public static final String BLESSING_ABSORPTION_PULSE = "blessing_absorption_pulse";
    public static final String FIRE_STORM_CAST_START = "fire_storm_cast_start";
    public static final String FIRE_STORM_CAST_BURST = "fire_storm_cast_burst";
    public static final String FIRE_STORM_CAST_STOP = "fire_storm_cast_stop";
    public static final String BASTION_FROST_AURA_START = "bastion_frost_aura_start";
    public static final String BASTION_FROST_AURA_STOP = "bastion_frost_aura_stop";
    public static final String BASTION_RULE_AURA_START = "bastion_rule_aura_start";
    public static final String BASTION_RULE_AURA_STOP = "bastion_rule_aura_stop";
    public static final String DIAMOND_SHARD_SPAWN = "diamond_shard_spawn";
    public static final String DIAMOND_SHARD_DESPAWN = "diamond_shard_despawn";
    public static final String DRAGON_FURY_CHARGE_SPAWN = "dragon_fury_charge_spawn";
    public static final String DRAGON_FURY_CHARGE_DESPAWN = "dragon_fury_charge_despawn";
    public static final String MANA_BUBBLE_CONTACT_BASIC = "mana_bubble_contact_basic";

    public static final String NETHERITE_BRIMSTONE_BLAST = "netherite_brimstone_blast";

    private WeaponVisualEffectHelper() {
    }

    public static void startDragonDescendCast(Entity entity) { play(entity, DRAGON_DESCEND_CAST_START); }
    public static void burstDragonDescendCast(Entity entity) { play(entity, DRAGON_DESCEND_CAST_BURST); }
    public static void stopDragonDescendCast(Entity entity) { play(entity, DRAGON_DESCEND_CAST_STOP); }
    public static void startGlacierCast(Entity entity) { play(entity, GLACIER_CAST_START); }
    public static void stopGlacierCast(Entity entity) { play(entity, GLACIER_CAST_STOP); }
    public static void playGlacierContactWave(Entity entity) { play(entity, GLACIER_CONTACT_WAVE); }
    public static void startBlessingCast(Entity entity) { play(entity, BLESSING_CAST_START); }
    public static void burstBlessingCast(Entity entity) { play(entity, BLESSING_CAST_BURST); }
    public static void stopBlessingCast(Entity entity) { play(entity, BLESSING_CAST_STOP); }
    public static void playBlessingHealPulse(Entity entity) { play(entity, BLESSING_HEAL_PULSE); }
    public static void playBlessingAbsorptionPulse(Entity entity) { play(entity, BLESSING_ABSORPTION_PULSE); }
    public static void startFireStormCast(Entity entity) { play(entity, FIRE_STORM_CAST_START); }
    public static void burstFireStormCast(Entity entity) { play(entity, FIRE_STORM_CAST_BURST); }
    public static void stopFireStormCast(Entity entity) { play(entity, FIRE_STORM_CAST_STOP); }
    public static void startBastionFrostAura(Entity entity) { play(entity, BASTION_FROST_AURA_START); }
    public static void stopBastionFrostAura(Entity entity) { play(entity, BASTION_FROST_AURA_STOP); }
    public static void startBastionRuleAura(Entity entity) { play(entity, BASTION_RULE_AURA_START); }
    public static void stopBastionRuleAura(Entity entity) { play(entity, BASTION_RULE_AURA_STOP); }
    public static void playDiamondShardSpawn(Entity entity) { play(entity, DIAMOND_SHARD_SPAWN); }
    public static void playDiamondShardDespawn(Entity entity) { play(entity, DIAMOND_SHARD_DESPAWN); }
    public static void playDragonFuryChargeSpawn(Entity entity) { play(entity, DRAGON_FURY_CHARGE_SPAWN); }
    public static void playDragonFuryChargeDespawn(Entity entity) { play(entity, DRAGON_FURY_CHARGE_DESPAWN); }
    public static void playManaBubbleBasicContact(Entity entity) { play(entity, MANA_BUBBLE_CONTACT_BASIC); }

    public static void playBrimstoneBlast(Entity entity) { play(entity, NETHERITE_BRIMSTONE_BLAST); }

    private static void play(Entity entity, String effectId) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        PBNetworkManager.sendToTrackingAndSelf(entity, new SPPlayWeaponVisualEffect(effectId, entity.getId()));
    }
}

