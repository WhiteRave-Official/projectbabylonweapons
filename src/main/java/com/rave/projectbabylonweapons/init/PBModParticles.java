package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PBModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ProjectBabylonWeapons.MODID);

    public static final RegistryObject<SimpleParticleType> BASIC_SPELL_PROJECTILE_TRAIL =
            PARTICLES.register("basic_spell_projectile_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ARCLIGHT_PROJECTILE_TRAIL =
            PARTICLES.register("arclight_projectile_trail", () -> new SimpleParticleType(true));

    private PBModParticles() {
    }
}
