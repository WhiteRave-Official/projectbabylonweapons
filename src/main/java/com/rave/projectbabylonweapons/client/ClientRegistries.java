package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.block.renderer.FrozenDebuffIceBlockTileRenderer;
import com.rave.projectbabylonweapons.client.particle.BasicSpellProjectileTrailParticle;
import com.rave.projectbabylonweapons.client.renderer.BasicSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.DiamondShardRenderer;
import com.rave.projectbabylonweapons.client.renderer.DragonFuryChargeRenderer;
import com.rave.projectbabylonweapons.client.renderer.DiamondSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.DragonDescendProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.GoldenSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.EnderSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.FireMagicalSealRenderer;
import com.rave.projectbabylonweapons.client.renderer.FireSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.FireStormEntityRenderer;
import com.rave.projectbabylonweapons.client.renderer.GlacierIceSpikeRenderer;
import com.rave.projectbabylonweapons.client.renderer.HolyMagicalSealRenderer;
import com.rave.projectbabylonweapons.client.renderer.HolySpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.IceSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.ManaBubbleProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.SickleChainRenderer;
import com.rave.projectbabylonweapons.client.renderer.TectonicFallingBlockRenderer;
import com.rave.projectbabylonweapons.init.PBModBlocks;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.init.PBModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistries {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PBModEntities.SICKLE_PROJECTILE.get(), SickleChainRenderer::new);
        event.registerEntityRenderer(PBModEntities.BASIC_SPELL_PROJECTILE.get(), BasicSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.GOLDEN_SPELL_PROJECTILE.get(), GoldenSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.DIAMOND_SPELL_PROJECTILE.get(), DiamondSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.DIAMOND_SHARD_1.get(), DiamondShardRenderer::new);
        event.registerEntityRenderer(PBModEntities.DIAMOND_SHARD_2.get(), DiamondShardRenderer::new);
        event.registerEntityRenderer(PBModEntities.DIAMOND_SHARD_3.get(), DiamondShardRenderer::new);
        event.registerEntityRenderer(PBModEntities.DRAGON_FURY_CHARGE.get(), DragonFuryChargeRenderer::new);
        event.registerEntityRenderer(PBModEntities.ICE_SPELL_PROJECTILE.get(), IceSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.FIRE_SPELL_PROJECTILE.get(), FireSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.HOLY_SPELL_PROJECTILE.get(), HolySpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.ENDER_SPELL_PROJECTILE.get(), EnderSpellProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.MANA_BUBBLE_PROJECTILE.get(), ManaBubbleProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.DRAGON_DESCEND_PROJECTILE.get(), DragonDescendProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.GLACIER_ICE_SPIKE.get(), GlacierIceSpikeRenderer::new);
        event.registerEntityRenderer(PBModEntities.HOLY_MAGICAL_SEAL.get(), HolyMagicalSealRenderer::new);
        event.registerEntityRenderer(PBModEntities.FIRE_MAGICAL_SEAL.get(), FireMagicalSealRenderer::new);
        event.registerEntityRenderer(PBModEntities.FIRE_STORM.get(), FireStormEntityRenderer::new);
        event.registerEntityRenderer(PBModEntities.TECTONIC_FALLING_BLOCK.get(), TectonicFallingBlockRenderer::new);
        event.registerBlockEntityRenderer(PBModBlocks.FROZEN_DEBUFF_ICE_BLOCK_ENTITY.get(), context -> new FrozenDebuffIceBlockTileRenderer());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(PBModParticles.BASIC_SPELL_PROJECTILE_TRAIL.get(), new BasicSpellProjectileTrailParticle.Provider());
    }
}
