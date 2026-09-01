package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.effect.ArclightRainPortalEntity;
import com.rave.projectbabylonweapons.world.entity.effect.DiamondShardEntity;
import com.rave.projectbabylonweapons.world.entity.effect.DragonFuryChargeEntity;
import com.rave.projectbabylonweapons.world.entity.effect.FireMagicalSealEntity;
import com.rave.projectbabylonweapons.world.entity.effect.FireStormEntity;
import com.rave.projectbabylonweapons.world.entity.effect.GlacierIceSpikeEntity;
import com.rave.projectbabylonweapons.world.entity.effect.HolyMagicalSealEntity;
import com.rave.projectbabylonweapons.world.entity.effect.TectonicFallingBlockEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.ArclightMiniProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.DiamondSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.DragonDescendProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.DragonsteelWyrmEchoProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.EnderSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.FireSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.GoldenSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.HolySpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.IceSpellProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.ManaBubbleProjectileEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ProjectBabylonWeapons.MODID);

    public static final RegistryObject<EntityType<SickleProjectileEntity>> SICKLE_PROJECTILE =
            ENTITIES.register("sickle_projectile", () ->
                    EntityType.Builder.<SickleProjectileEntity>of(SickleProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("sickle_projectile"));

    public static final RegistryObject<EntityType<BasicSpellProjectileEntity>> BASIC_SPELL_PROJECTILE =
            ENTITIES.register("basic_spell_projectile", () ->
                    EntityType.Builder.<BasicSpellProjectileEntity>of(BasicSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("basic_spell_projectile"));

    public static final RegistryObject<EntityType<GoldenSpellProjectileEntity>> GOLDEN_SPELL_PROJECTILE =
            ENTITIES.register("golden_spell_projectile", () ->
                    EntityType.Builder.<GoldenSpellProjectileEntity>of(GoldenSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("golden_spell_projectile"));

    public static final RegistryObject<EntityType<DiamondSpellProjectileEntity>> DIAMOND_SPELL_PROJECTILE =
            ENTITIES.register("diamond_spell_projectile", () ->
                    EntityType.Builder.<DiamondSpellProjectileEntity>of(DiamondSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("diamond_spell_projectile"));
    public static final RegistryObject<EntityType<IceSpellProjectileEntity>> ICE_SPELL_PROJECTILE =
            ENTITIES.register("ice_spell_projectile", () ->
                    EntityType.Builder.<IceSpellProjectileEntity>of(IceSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("ice_spell_projectile"));

    public static final RegistryObject<EntityType<FireSpellProjectileEntity>> FIRE_SPELL_PROJECTILE =
            ENTITIES.register("fire_spell_projectile", () ->
                    EntityType.Builder.<FireSpellProjectileEntity>of(FireSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("fire_spell_projectile"));

    public static final RegistryObject<EntityType<HolySpellProjectileEntity>> HOLY_SPELL_PROJECTILE =
            ENTITIES.register("holy_spell_projectile", () ->
                    EntityType.Builder.<HolySpellProjectileEntity>of(HolySpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("holy_spell_projectile"));

    public static final RegistryObject<EntityType<EnderSpellProjectileEntity>> ENDER_SPELL_PROJECTILE =
            ENTITIES.register("ender_spell_projectile", () ->
                    EntityType.Builder.<EnderSpellProjectileEntity>of(EnderSpellProjectileEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("ender_spell_projectile"));

    public static final RegistryObject<EntityType<ManaBubbleProjectileEntity>> MANA_BUBBLE_PROJECTILE =
            ENTITIES.register("mana_bubble_projectile", () ->
                    EntityType.Builder.<ManaBubbleProjectileEntity>of(ManaBubbleProjectileEntity::new, MobCategory.MISC)
                            .sized(4.5f, 4.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("mana_bubble_projectile"));

    public static final RegistryObject<EntityType<DragonDescendProjectileEntity>> DRAGON_DESCEND_PROJECTILE =
            ENTITIES.register("dragon_descend_projectile", () ->
                    EntityType.Builder.<DragonDescendProjectileEntity>of(DragonDescendProjectileEntity::new, MobCategory.MISC)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dragon_descend_projectile"));


    public static final RegistryObject<EntityType<DragonsteelWyrmEchoProjectileEntity>> DRAGONSTEEL_WYRM_ECHO_PROJECTILE =
            ENTITIES.register("dragonsteel_wyrm_echo_projectile", () ->
                    EntityType.Builder.<DragonsteelWyrmEchoProjectileEntity>of(DragonsteelWyrmEchoProjectileEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dragonsteel_wyrm_echo_projectile"));
    public static final RegistryObject<EntityType<ArclightRainPortalEntity>> ARCLIGHT_RAIN_PORTAL =
            ENTITIES.register("arclight_rain_portal", () ->
                    EntityType.Builder.<ArclightRainPortalEntity>of(ArclightRainPortalEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("arclight_rain_portal"));
    public static final RegistryObject<EntityType<ArclightMiniProjectileEntity>> ARCLIGHT_MINI_PROJECTILE =
            ENTITIES.register("arclight_mini_projectile", () ->
                    EntityType.Builder.<ArclightMiniProjectileEntity>of(ArclightMiniProjectileEntity::new, MobCategory.MISC)
                            .sized(0.525F, 0.525F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("arclight_mini_projectile"));

    public static final RegistryObject<EntityType<ArclightMiniProjectileEntity>> ARCLIGHT_SPEAR_PROJECTILE =
            ENTITIES.register("arclight_spear_projectile", () ->
                    EntityType.Builder.<ArclightMiniProjectileEntity>of(ArclightMiniProjectileEntity::new, MobCategory.MISC)
                            .sized(1.65F, 1.65F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("arclight_spear_projectile"));
    public static final RegistryObject<EntityType<GlacierIceSpikeEntity>> GLACIER_ICE_SPIKE =
            ENTITIES.register("glacier_ice_spike", () ->
                    EntityType.Builder.<GlacierIceSpikeEntity>of(GlacierIceSpikeEntity::new, MobCategory.MISC)
                            .sized(1.6f, 2.4f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("glacier_ice_spike"));

    public static final RegistryObject<EntityType<HolyMagicalSealEntity>> HOLY_MAGICAL_SEAL =
            ENTITIES.register("holy_magical_seal", () ->
                    EntityType.Builder.<HolyMagicalSealEntity>of(HolyMagicalSealEntity::new, MobCategory.MISC)
                            .sized(3.0f, 0.2f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("holy_magical_seal"));

    public static final RegistryObject<EntityType<FireMagicalSealEntity>> FIRE_MAGICAL_SEAL =
            ENTITIES.register("fire_magical_seal", () ->
                    EntityType.Builder.<FireMagicalSealEntity>of(FireMagicalSealEntity::new, MobCategory.MISC)
                            .sized(3.0f, 0.2f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("fire_magical_seal"));

    public static final RegistryObject<EntityType<FireStormEntity>> FIRE_STORM =
            ENTITIES.register("fire_storm", () ->
                    EntityType.Builder.<FireStormEntity>of(FireStormEntity::new, MobCategory.MISC)
                            .sized(3.0f, 4.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("fire_storm"));

    public static final RegistryObject<EntityType<DiamondShardEntity>> DIAMOND_SHARD_1 =
            ENTITIES.register("diamond_shard_1", () ->
                    EntityType.Builder.<DiamondShardEntity>of(DiamondShardEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("diamond_shard_1"));

    public static final RegistryObject<EntityType<DiamondShardEntity>> DIAMOND_SHARD_2 =
            ENTITIES.register("diamond_shard_2", () ->
                    EntityType.Builder.<DiamondShardEntity>of(DiamondShardEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("diamond_shard_2"));

    public static final RegistryObject<EntityType<DiamondShardEntity>> DIAMOND_SHARD_3 =
            ENTITIES.register("diamond_shard_3", () ->
                    EntityType.Builder.<DiamondShardEntity>of(DiamondShardEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("diamond_shard_3"));

    public static final RegistryObject<EntityType<DragonFuryChargeEntity>> DRAGON_FURY_CHARGE =
            ENTITIES.register("dragon_fury_charge", () ->
                    EntityType.Builder.<DragonFuryChargeEntity>of(DragonFuryChargeEntity::new, MobCategory.MISC)
                            .sized(0.75f, 0.75f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dragon_fury_charge"));

    public static final RegistryObject<EntityType<TectonicFallingBlockEntity>> TECTONIC_FALLING_BLOCK =
            ENTITIES.register("tectonic_falling_block", () ->
                    EntityType.Builder.<TectonicFallingBlockEntity>of(TectonicFallingBlockEntity::new, MobCategory.MISC)
                            .sized(0.98f, 0.98f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("tectonic_falling_block"));
}
