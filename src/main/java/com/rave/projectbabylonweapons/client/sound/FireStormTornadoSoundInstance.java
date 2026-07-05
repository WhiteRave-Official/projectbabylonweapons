package com.rave.projectbabylonweapons.client.sound;

import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.world.entity.effect.FireStormEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class FireStormTornadoSoundInstance extends AbstractTickableSoundInstance {
    private static final float TARGET_VOLUME = 1.4F;
    private static final float FADE_IN_STEP = 0.12F;
    private static final float FADE_OUT_STEP = 0.18F;

    private final int entityId;
    private FireStormEntity entity;
    private boolean stopped;
    private boolean fadingOut;

    public FireStormTornadoSoundInstance(FireStormEntity entity) {
        super(PBWSounds.FIRE_TORNADO.get(), SoundSource.HOSTILE, RandomSource.create());
        this.entity = entity;
        this.entityId = entity.getId();
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.volume = 0.05F;
        this.pitch = 1.0F;
        this.attenuation = Attenuation.LINEAR;
        this.x = entity.getX();
        this.y = entity.getY() + 0.5D;
        this.z = entity.getZ();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void tick() {
        FireStormEntity current = this.entity;
        if (current == null || !current.isAlive() || current.isRemoved()) {
            this.fadingOut = true;
        } else {
            this.x = current.getX();
            this.y = current.getY() + 0.5D;
            this.z = current.getZ();
        }

        if (this.fadingOut) {
            this.volume = Math.max(0.0F, this.volume - FADE_OUT_STEP);
            if (this.volume <= 0.001F) {
                this.stop();
                this.stopped = true;
            }
            return;
        }

        this.volume = Math.min(TARGET_VOLUME, this.volume + FADE_IN_STEP);
    }
}

