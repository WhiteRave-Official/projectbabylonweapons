package com.rave.projectbabylonweapons.passive.smallshield;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.world.entity.effect.DiamondShardEntity;
import com.rave.projectbabylonweapons.world.entity.effect.DragonFuryChargeEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.corruptdog.cdm.gameasset.CDSkills;
import net.corruptdog.cdm.gameasset.CorruptAnimations;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.event.types.entity.DealDamageEvent;
import yesman.epicfight.api.event.types.entity.TakeDamageEvent;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SmallShieldPassiveHandler {
    private static final IdentifierProvider SKILL_CAST_LISTENER = IdentifierProvider.constant(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "smallshield_skill_cast"));
    private static final IdentifierProvider TAKE_DAMAGE_LISTENER = IdentifierProvider.constant(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "smallshield_take_damage"));
    private static final IdentifierProvider DEAL_DAMAGE_LISTENER = IdentifierProvider.constant(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "smallshield_deal_damage"));
    private static final double LAUNCH_TARGET_HEIGHT = 1.05D;
    private static final float SHIELD_BASH_PARRY_WINDOW_SECONDS = 0.2F;

    private static final Map<UUID, Boolean> REGISTERED_PLAYERS = new HashMap<>();
    private static final Map<UUID, Integer> DRAGON_FURY_CHARGES = new HashMap<>();
    private static final Map<UUID, AbsorptionGrantState> ETHEREAL_ABSORPTION = new HashMap<>();

    private SmallShieldPassiveHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Player player = event.getEntity();
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch == null) {
            return;
        }

        UUID playerId = player.getUUID();
        if (REGISTERED_PLAYERS.putIfAbsent(playerId, Boolean.TRUE) == null) {
            playerPatch.getEventListener().registerContextAwareEvent(EpicFightEventHooks.Player.CAST_SKILL,
                    (SkillCastEvent skillCastEvent, yesman.epicfight.api.event.EventContext context) -> onSkillCast(player, skillCastEvent), SKILL_CAST_LISTENER);
            playerPatch.getEventListener().registerContextAwareEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
                    (TakeDamageEvent.Income takeDamageEvent, yesman.epicfight.api.event.EventContext context) -> onTakeDamageAttack(player, takeDamageEvent), TAKE_DAMAGE_LISTENER);
            playerPatch.getEventListener().registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_PRE,
                    (DealDamageEvent.Pre dealDamageEvent) -> onDealDamageHurt(player, dealDamageEvent), DEAL_DAMAGE_LISTENER);
        }

        tickTemporaryStates(player);
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        REGISTERED_PLAYERS.remove(playerId);
        DRAGON_FURY_CHARGES.remove(playerId);
        ETHEREAL_ABSORPTION.remove(playerId);

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);
        if (playerPatch != null) {
            playerPatch.getEventListener().removeListenersBelongTo(SKILL_CAST_LISTENER);
            playerPatch.getEventListener().removeListenersBelongTo(TAKE_DAMAGE_LISTENER);
            playerPatch.getEventListener().removeListenersBelongTo(DEAL_DAMAGE_LISTENER);
        }
    }

    private static void onSkillCast(Player player, SkillCastEvent event) {
        if (player.level().isClientSide || event.isCanceled() || event.getSkillContainer() == null || event.getSkillContainer().getSkill() == null) {
            return;
        }

        if (event.getSkillContainer().getSkill() != CDSkills.SHILEDSLASH) {
            return;
        }

        ItemStack shieldStack = getEquippedSmallShieldStack(player);
        if (shieldStack.isEmpty()) {
            return;
        }

        DiamondSmallShieldBalance.Profile diamond = DiamondSmallShieldBalance.resolve(shieldStack);
        if (diamond != null && player.level() instanceof ServerLevel serverLevel) {
            launchDiamondShards(serverLevel, player, diamond);
        }
    }

    private static void onTakeDamageAttack(Player player, TakeDamageEvent.Income event) {
        ItemStack shieldStack = getEquippedSmallShieldStack(player);
        if (shieldStack.isEmpty()) {
            return;
        }

        boolean ordinaryBlocked = event.getResult() == AttackResult.ResultType.BLOCKED;
        boolean vanillaShieldBlock = isVanillaShieldBlock(player, event);
        boolean shieldBashParry = isShieldBashParry(event);

        DiamondSmallShieldBalance.Profile diamond = DiamondSmallShieldBalance.resolve(shieldStack);
        if (diamond != null && (ordinaryBlocked || vanillaShieldBlock || shieldBashParry) && player.level() instanceof ServerLevel serverLevel) {
            spawnDiamondShard(serverLevel, player, diamond);
        }

        if (!shieldBashParry) {
            return;
        }

        LivingEntity attacker = resolveLivingAttacker(event.getDamageSource());
        if (attacker == null || attacker == player || !attacker.isAlive()) {
            return;
        }

        GoldenSmallShieldBalance.Profile golden = GoldenSmallShieldBalance.resolve(shieldStack);
        if (golden != null) {
            attacker.addEffect(new MobEffectInstance(PBMEffects.EXHAUSTED, golden.exhaustedDurationTicks(), 0, false, true, true));
        }

        EtherealSmallShieldBalance.Profile ethereal = EtherealSmallShieldBalance.resolve(shieldStack);
        if (ethereal != null) {
            int removedBuffs = removeBeneficialEffects(attacker);
            if (removedBuffs > 0) {
                float restoreAmount = ethereal.weaponChargeRestorePerBuff() * removedBuffs;
                float absorptionAmount = player.getMaxHealth() * ethereal.absorptionPercentPerBuff() * removedBuffs;
                restoreWeaponInnateCharge((ServerPlayerPatch) event.getEntityPatch(), restoreAmount);
                grantTemporaryAbsorption(player, absorptionAmount, ethereal.absorptionDurationTicks());
            }
        }

        IceSmallShieldBalance.Profile ice = IceSmallShieldBalance.resolve(shieldStack);
        if (ice != null) {
            attacker.addEffect(new MobEffectInstance(PBMEffects.FROZEN, ice.frozenDurationTicks(), 0, false, true, true));
            attacker.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, ice.chillDurationTicks(), 0, false, true, true));
        }

        NetheriteSmallShieldBalance.Profile netherite = NetheriteSmallShieldBalance.resolve(shieldStack);
        if (netherite != null) {
            attacker.igniteForSeconds(netherite.fireDurationSeconds());
            attacker.addEffect(new MobEffectInstance(PBMEffects.BRIMSTONE_FLAMES, netherite.brimstoneDurationTicks(), 0, false, true, true));
        }

        DragonsteelSmallShieldBalance.Profile dragonsteel = DragonsteelSmallShieldBalance.resolve(shieldStack);
        if (dragonsteel != null) {
            int nextCharges = Math.min(dragonsteel.maxCharges(), DRAGON_FURY_CHARGES.getOrDefault(player.getUUID(), 0) + 1);
            DRAGON_FURY_CHARGES.put(player.getUUID(), nextCharges);
        }
    }

    private static void onDealDamageHurt(Player player, DealDamageEvent.Pre event) {
        if (player.level().isClientSide || event.getTarget() == null || !event.getTarget().isAlive() || event.getTarget().isAlliedTo(player)) {
            return;
        }

        applyDragonFuryIfPresent(player, event);
    }

    private static void applyDragonFuryIfPresent(Player player, DealDamageEvent.Pre event) {
        DragonsteelSmallShieldBalance.Profile profile = DragonsteelSmallShieldBalance.resolve(getEquippedSmallShieldStack(player));
        if (profile == null) {
            DRAGON_FURY_CHARGES.remove(player.getUUID());
            return;
        }

        if (isShieldBashAnimation(event)) {
            return;
        }

        int charges = DRAGON_FURY_CHARGES.getOrDefault(player.getUUID(), 0);
        if (charges <= 0) {
            return;
        }

        ItemStack usedItem = event.getDamageSource().getUsedItem();
        if (usedItem.isEmpty() || usedItem.getItem() == player.getOffhandItem().getItem()) {
            return;
        }

        event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + (profile.damageBonusMultiplierPerCharge() * charges)));
        event.getDamageSource().attachArmorNegationModifier(ValueModifier.adder(profile.armorNegationPerCharge() * charges));
        DRAGON_FURY_CHARGES.remove(player.getUUID());
    }

    private static void tickTemporaryStates(Player player) {
        UUID playerId = player.getUUID();

        DragonsteelSmallShieldBalance.Profile dragonsteelProfile = DragonsteelSmallShieldBalance.resolve(getEquippedSmallShieldStack(player));
        if (dragonsteelProfile == null) {
            DRAGON_FURY_CHARGES.remove(playerId);
            discardDragonFuryCharges(player);
        } else if (player.level() instanceof ServerLevel serverLevel) {
            synchronizeDragonFuryCharges(serverLevel, player, DRAGON_FURY_CHARGES.getOrDefault(playerId, 0));
        }

        AbsorptionGrantState state = ETHEREAL_ABSORPTION.get(playerId);
        if (state == null) {
            return;
        }

        if (!player.isAlive()) {
            ETHEREAL_ABSORPTION.remove(playerId);
            return;
        }

        if (player.level().getGameTime() >= state.expiresAtGameTime) {
            float currentAbsorption = player.getAbsorptionAmount();
            float ownedContributionPresent = Math.min(currentAbsorption, state.amount);
            player.setAbsorptionAmount(Math.max(0.0F, currentAbsorption - ownedContributionPresent));
            ETHEREAL_ABSORPTION.remove(playerId);
        }
    }

    private static boolean isShieldBashParry(TakeDamageEvent.Income event) {
        ServerPlayerPatch playerPatch = (ServerPlayerPatch) event.getEntityPatch();
        AnimationPlayer animationPlayer = playerPatch.getAnimator().getPlayerFor(null);
        if (animationPlayer == null || animationPlayer.getRealAnimation() == null) {
            return false;
        }

        if (animationPlayer.getElapsedTime() > SHIELD_BASH_PARRY_WINDOW_SECONDS) {
            return false;
        }

        AssetAccessor<?> currentAnimation = animationPlayer.getRealAnimation();
        if (!currentAnimation.toString().equals(CorruptAnimations.SHILED_SLASH.registryName().toString())) {
            return false;
        }

        return isFrontBlockableSource(playerPatch, event.getDamageSource());
    }

    private static boolean isShieldBashAnimation(DealDamageEvent.Pre event) {
        var animation = event.getDamageSource().getAnimation();
        return animation != null && animation.toString().equals(CorruptAnimations.SHILED_SLASH.registryName().toString());
    }

    private static boolean isVanillaShieldBlock(Player player, TakeDamageEvent.Income event) {
        if (!player.isBlocking()) {
            return false;
        }

        return isFrontBlockableSource((ServerPlayerPatch) event.getEntityPatch(), event.getDamageSource());
    }

    private static boolean isFrontBlockableSource(ServerPlayerPatch playerPatch, DamageSource damageSource) {
        Vec3 sourceLocation = damageSource.getSourcePosition();
        if (sourceLocation == null) {
            return false;
        }

        Vec3 viewVector = playerPatch.getOriginal().getViewVector(1.0F);
        viewVector = viewVector.subtract(0.0D, viewVector.y, 0.0D);
        if (viewVector.lengthSqr() < 1.0E-6D) {
            return false;
        }
        viewVector = viewVector.normalize();

        Vec3 toSourceLocation = sourceLocation.subtract(playerPatch.getOriginal().position());
        if (toSourceLocation.lengthSqr() < 1.0E-6D) {
            return false;
        }
        toSourceLocation = toSourceLocation.normalize();

        if (toSourceLocation.dot(viewVector) <= 0.0D) {
            return false;
        }

        return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !damageSource.is(DamageTypes.MAGIC)
                && !damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
                && !damageSource.is(DamageTypeTags.IS_PROJECTILE)
                && !damageSource.is(DamageTypeTags.IS_EXPLOSION)
                && !damageSource.is(DamageTypeTags.IS_FIRE);
    }


    private static void synchronizeDragonFuryCharges(ServerLevel level, Player player, int charges) {
        List<DragonFuryChargeEntity> activeCharges = getActiveDragonFuryCharges(level, player);
        if (charges <= 0) {
            for (DragonFuryChargeEntity charge : activeCharges) {
                charge.discardWithEffects();
            }
            return;
        }

        while (activeCharges.size() > charges) {
            DragonFuryChargeEntity extraCharge = activeCharges.remove(activeCharges.size() - 1);
            extraCharge.discardWithEffects();
        }

        while (activeCharges.size() < charges) {
            DragonFuryChargeEntity chargeEntity = new DragonFuryChargeEntity(PBModEntities.DRAGON_FURY_CHARGE.get(), level);
            activeCharges.add(chargeEntity);
            chargeEntity.initializeOrbit(player, activeCharges.size() - 1, charges);
            level.addFreshEntity(chargeEntity);
        }

        reindexDragonFuryCharges(activeCharges, charges);
    }

    private static void discardDragonFuryCharges(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (DragonFuryChargeEntity charge : getActiveDragonFuryCharges(serverLevel, player)) {
            charge.discardWithEffects();
        }
    }

    private static List<DragonFuryChargeEntity> getActiveDragonFuryCharges(ServerLevel level, Player player) {
        return new ArrayList<>(level.getEntitiesOfClass(DragonFuryChargeEntity.class,
                player.getBoundingBox().inflate(24.0D),
                entity -> entity.isAlive() && player.getUUID().equals(entity.getOwnerUuid())));
    }

    private static void reindexDragonFuryCharges(List<DragonFuryChargeEntity> charges, int total) {
        charges.sort(Comparator.comparingInt(Entity::getId));
        for (int i = 0; i < charges.size(); i++) {
            charges.get(i).setOrbitData(i, total);
        }
    }
    private static void spawnDiamondShard(ServerLevel serverLevel, Player player, DiamondSmallShieldBalance.Profile profile) {
        List<DiamondShardEntity> activeShards = getActiveOrbitingShards(serverLevel, player);
        if (activeShards.size() >= profile.maxShards()) {
            DiamondShardEntity oldestShard = activeShards.stream().min(Comparator.comparingInt(entity -> entity.tickCount)).orElse(null);
            if (oldestShard != null) {
                oldestShard.discardWithEffects();
                activeShards.remove(oldestShard);
            }
        }

        DiamondShardEntity shard = createRandomShard(serverLevel);
        shard.setLifetimeTicks(profile.shardLifetimeTicks());
        shard.setWeaponChipDurationTicks(profile.weaponChipDurationTicks());
        activeShards.add(shard);
        shard.initializeOrbit(player, activeShards.size() - 1, activeShards.size());
        serverLevel.addFreshEntity(shard);
        reindexOrbitingShards(activeShards);
    }

    private static void launchDiamondShards(ServerLevel serverLevel, Player player, DiamondSmallShieldBalance.Profile profile) {
        List<DiamondShardEntity> activeShards = getActiveOrbitingShards(serverLevel, player);
        if (activeShards.isEmpty()) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = player.getForward();
        } else {
            forward = forward.normalize();
        }

        Vec3 targetPoint = player.position().add(0.0D, LAUNCH_TARGET_HEIGHT, 0.0D).add(forward.scale(profile.launchDistance()));
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * profile.damageMultiplier();
        for (DiamondShardEntity shard : activeShards) {
            shard.setDamageAmount(damage);
            shard.setLifetimeTicks(profile.shardLifetimeTicks());
            shard.setWeaponChipDurationTicks(profile.weaponChipDurationTicks());
            shard.launch(targetPoint, profile.launchSpeed());
        }
    }

    private static List<DiamondShardEntity> getActiveOrbitingShards(ServerLevel level, Player player) {
        return new ArrayList<>(level.getEntitiesOfClass(DiamondShardEntity.class, player.getBoundingBox().inflate(24.0D), entity -> entity.isAlive() && !entity.isLaunched() && player.getUUID().equals(entity.getOwnerUuid())));
    }

    private static void reindexOrbitingShards(List<DiamondShardEntity> shards) {
        shards.sort(Comparator.comparingInt(Entity::getId));
        int total = shards.size();
        for (int i = 0; i < total; i++) {
            shards.get(i).setOrbitData(i, total);
        }
    }

    private static DiamondShardEntity createRandomShard(ServerLevel level) {
        int variant = level.random.nextInt(3);
        if (variant == 1) {
            return new DiamondShardEntity(PBModEntities.DIAMOND_SHARD_2.get(), level);
        }
        if (variant == 2) {
            return new DiamondShardEntity(PBModEntities.DIAMOND_SHARD_3.get(), level);
        }
        return new DiamondShardEntity(PBModEntities.DIAMOND_SHARD_1.get(), level);
    }

    private static ItemStack getEquippedSmallShieldStack(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (isSupportedSmallShield(offhand)) {
            return offhand;
        }

        ItemStack mainhand = player.getMainHandItem();
        if (isSupportedSmallShield(mainhand)) {
            return mainhand;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isSupportedSmallShield(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return DiamondSmallShieldBalance.resolve(stack) != null
                || GoldenSmallShieldBalance.resolve(stack) != null
                || IceSmallShieldBalance.resolve(stack) != null
                || NetheriteSmallShieldBalance.resolve(stack) != null
                || EtherealSmallShieldBalance.resolve(stack) != null
                || DragonsteelSmallShieldBalance.resolve(stack) != null;
    }
    private static LivingEntity resolveLivingAttacker(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof LivingEntity living) {
            return living;
        }
        if (damageSource.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private static int removeBeneficialEffects(LivingEntity target) {
        List<Holder<MobEffect>> removableEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : target.getActiveEffects()) {
            if (effectInstance.getEffect().value().isBeneficial()) {
                removableEffects.add(effectInstance.getEffect());
            }
        }
        removableEffects.forEach(target::removeEffect);
        return removableEffects.size();
    }

    private static void restoreWeaponInnateCharge(PlayerPatch<?> playerPatch, float percentOfMaxResource) {
        if (percentOfMaxResource <= 0.0F) {
            return;
        }

        SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        if (container == null || container.getSkill() == null) {
            return;
        }

        float restoreAmount = container.getMaxResource() * percentOfMaxResource;
        container.getSkill().setConsumptionSynchronize(container, container.getResource() + restoreAmount);
    }

    private static void grantTemporaryAbsorption(Player player, float amount, int durationTicks) {
        if (!(player instanceof ServerPlayer) || amount <= 0.0F || durationTicks <= 0) {
            return;
        }

        UUID playerId = player.getUUID();
        AbsorptionGrantState previousState = ETHEREAL_ABSORPTION.get(playerId);
        float currentAbsorption = player.getAbsorptionAmount();
        if (previousState != null) {
            float ownedContributionPresent = Math.min(currentAbsorption, previousState.amount);
            currentAbsorption = Math.max(0.0F, currentAbsorption - ownedContributionPresent);
        }

        float totalAmount = (previousState != null ? previousState.amount : 0.0F) + amount;
        player.setAbsorptionAmount(currentAbsorption + totalAmount);
        ETHEREAL_ABSORPTION.put(playerId, new AbsorptionGrantState(totalAmount, player.level().getGameTime() + durationTicks));
    }

    private record AbsorptionGrantState(float amount, long expiresAtGameTime) {
    }
}