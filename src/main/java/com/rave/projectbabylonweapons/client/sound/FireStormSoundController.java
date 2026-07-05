package com.rave.projectbabylonweapons.client.sound;

import com.rave.projectbabylonweapons.world.entity.effect.FireStormEntity;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class FireStormSoundController {
    private static final Map<Integer, FireStormTornadoSoundInstance> ACTIVE_SOUNDS = new HashMap<>();

    private FireStormSoundController() {
    }

    public static void ensurePlaying(FireStormEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getSoundManager() == null) {
            return;
        }

        cleanupStopped();

        FireStormTornadoSoundInstance existing = ACTIVE_SOUNDS.get(entity.getId());
        if (existing != null && !existing.isStopped()) {
            return;
        }

        FireStormTornadoSoundInstance soundInstance = new FireStormTornadoSoundInstance(entity);
        ACTIVE_SOUNDS.put(entity.getId(), soundInstance);
        minecraft.getSoundManager().play(soundInstance);
    }

    private static void cleanupStopped() {
        Iterator<Map.Entry<Integer, FireStormTornadoSoundInstance>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isStopped()) {
                iterator.remove();
            }
        }
    }
}
