package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.block.renderer.FrozenDebuffIceBlockTileRenderer;
import com.rave.projectbabylonweapons.client.particle.BasicSpellProjectileTrailParticle;
import com.rave.projectbabylonweapons.client.tooltip.EvergateNameClientTooltip;
import com.rave.projectbabylonweapons.tooltip.EvergateNameTooltipData;
import com.rave.projectbabylonweapons.client.renderer.ArclightMiniProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.ArclightSummonedWeaponRenderer;
import com.rave.projectbabylonweapons.client.renderer.PatchedArclightSummonedWeaponRenderer;
import com.rave.projectbabylonweapons.client.renderer.ArclightRainPortalRenderer;
import com.rave.projectbabylonweapons.client.renderer.BasicSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.item.ArclightAwakeningItemRenderer;
import com.rave.projectbabylonweapons.client.renderer.DiamondShardRenderer;
import com.rave.projectbabylonweapons.client.renderer.DragonFuryChargeRenderer;
import com.rave.projectbabylonweapons.client.renderer.DiamondSpellProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.DragonDescendProjectileRenderer;
import com.rave.projectbabylonweapons.client.renderer.DragonsteelWyrmEchoProjectileRenderer;
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
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.init.PBModParticles;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientRegistries {
    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EvergateNameTooltipData.class, EvergateNameClientTooltip::new);
    }

    @SubscribeEvent
    public static void registerItemRenderers(PatchedRenderersEvent.RegisterItemRenderer event) {
        event.addItemRenderer(
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "arclight_awakening"),
                ArclightAwakeningItemRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerPatchedRenderers(PatchedRenderersEvent.Add event) {
        event.addPatchedEntityRenderer(PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(),
                entityType -> new PatchedArclightSummonedWeaponRenderer(event.getContext(), entityType));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ArclightMiniProjectileRenderer.MINI_MODEL);
        event.register(ArclightMiniProjectileRenderer.SPEAR_MODEL);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PBModEntities.SICKLE_PROJECTILE.get(), SickleChainRenderer::new);
        event.registerEntityRenderer(PBModEntities.ARCLIGHT_RAIN_PORTAL.get(), ArclightRainPortalRenderer::new);
        event.registerEntityRenderer(PBModEntities.ARCLIGHT_MINI_PROJECTILE.get(), ArclightMiniProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.ARCLIGHT_SPEAR_PROJECTILE.get(), ArclightMiniProjectileRenderer::new);
        event.registerEntityRenderer(PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(), ArclightSummonedWeaponRenderer::new);
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
        event.registerEntityRenderer(PBModEntities.DRAGONSTEEL_WYRM_ECHO_PROJECTILE.get(), DragonsteelWyrmEchoProjectileRenderer::new);
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
        event.registerSpecial(PBModParticles.ARCLIGHT_PROJECTILE_TRAIL.get(), new BasicSpellProjectileTrailParticle.ArclightProvider());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                PBModItems.ARCLIGHT_SWORD.get(),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "evergate_form"),
                (stack, level, entity, seed) -> ArclightSwordItem.isEvergate(stack) ? 1.0F : 0.0F));
    }
}
