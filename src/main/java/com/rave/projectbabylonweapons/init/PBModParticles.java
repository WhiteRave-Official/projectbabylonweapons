package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PBModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ProjectBabylonWeapons.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BASIC_SPELL_PROJECTILE_TRAIL =
            PARTICLES.register("basic_spell_projectile_trail", () -> new SimpleParticleType(true));

    private PBModParticles() {
    }
}
