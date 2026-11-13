package com.rave.projectbabylonweapons.skill.arclight;

import com.rave.projectbabylonweapons.init.PBWSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;


public final class LightBreachSounds {
    private LightBreachSounds() {}

    public static void seals(ServerLevel level, Vec3 center) {
        play(level, center, PBWSounds.LIGHT_BREACH_SEAL.get(), 0.65F);
    }

    public static void beam(ServerLevel level, Vec3 position, int columnCount) {

        float volume = 0.8F / (float) Math.sqrt(Math.max(1, columnCount));
        play(level, position, PBWSounds.LIGHT_BREACH_BEAM.get(), volume);
    }

    private static void play(ServerLevel level, Vec3 position, SoundEvent sound, float volume) {
        float pitch = 0.95F + level.random.nextFloat() * 0.10F;
        level.playSound(null, position.x, position.y, position.z, sound, SoundSource.PLAYERS, volume, pitch);
    }
}
