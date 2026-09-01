package com.rave.projectbabylonweapons.passive.special;

import com.rave.projectbabylonmaterials.combat.EnchantmentRebalanceHelper;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.network.SPBarrierSync;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.AnimationBeginEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArclightFormPassiveHandler {
    private static final UUID ANIMATION_BEGIN_LISTENER = UUID.fromString("bdc8b0f9-b668-42ee-a376-7e6d2d49c5a4");
    private static final UUID MANA_REGEN_MODIFIER = UUID.fromString("279ab75f-b1f4-4305-ac30-11ad13fbb430");
    private static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("b8b6e084-b34d-4ad7-8137-0d9fc88ccf3e");
    private static final String MANA_REGEN_MODIFIER_NAME = ProjectBabylonWeapons.MODID + ".passive.conduit.mana_regeneration";
    private static final String MOVEMENT_SPEED_MODIFIER_NAME = ProjectBabylonWeapons.MODID + ".passive.unity.movement_speed";
    private static final int EMPOWERED_ATTACK_WINDOW_TICKS = 40;

    private static final Set<UUID> REGISTERED_PLAYERS = new HashSet<>();
    private static final Map<UUID, Long> EMPOWERED_ATTACKS = new HashMap<>();
    private static final Map<UUID, BarrierState> BARRIERS = new HashMap<>();
    private static final ThreadLocal<Set<UUID>> PROCESSING_HOLY_DAMAGE = ThreadLocal.withInitial(HashSet::new);

    private ArclightFormPassiveHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        registerAnimationListener(player);
        refreshFormAttributes(player);
        tickBarrier(player);
        Long empoweredUntil = EMPOWERED_ATTACKS.get(player.getUUID());
        if (empoweredUntil != null && player.level().getGameTime() > empoweredUntil) {
            EMPOWERED_ATTACKS.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        absorbBarrierDamage(event);

        if (event.getEntity().level().isClientSide
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getSource().getDirectEntity() != attacker
                || PROCESSING_HOLY_DAMAGE.get().contains(event.getEntity().getUUID())) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        float originalDamage = event.getAmount();
        float holyDamage;

        ArclightConduitBalance.Profile conduit = ArclightConduitBalance.resolve(weapon);
        if (conduit != null) {
            Long empoweredUntil = EMPOWERED_ATTACKS.get(attacker.getUUID());
            if (empoweredUntil != null && attacker.level().getGameTime() <= empoweredUntil) {
                event.setAmount(originalDamage * (1.0F + conduit.damageBonus()));
            }
            holyDamage = originalDamage * conduit.holyDamagePercent();
        } else {
            EvergateUnityBalance.Profile unity = EvergateUnityBalance.resolve(weapon);
            if (unity == null) {
                return;
            }
            holyDamage = originalDamage * unity.holyDamagePercent();
        }

        dealHolyDamage(attacker, event.getEntity(), holyDamage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && EnchantmentRebalanceHelper.isMagicDamage(event.getSource())) {
            ArclightConduitBalance.Profile conduit = ArclightConduitBalance.resolve(attacker.getMainHandItem());
            if (conduit != null && event.getAmount() > 0.0F) {
                addBarrier(attacker, event.getAmount() * conduit.barrierConversionPercent(),
                        conduit.barrierDurationTicks());
            }
        }


    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer watcher)
                || !(event.getTarget() instanceof ServerPlayer target)) {
            return;
        }

        BarrierState barrier = BARRIERS.get(target.getUUID());
        if (barrier != null && barrier.amount > 0.0F && target.level().getGameTime() < barrier.expiresAt) {
            PBNetworkManager.sendToPlayer(watcher, new SPBarrierSync(target.getId(), barrier.amount));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        cleanupPlayer(event.getOriginal());
        cleanupPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        cleanupPlayer(event.getEntity());
    }

    private static void registerAnimationListener(ServerPlayer player) {
        if (!REGISTERED_PLAYERS.add(player.getUUID())) {
            return;
        }

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch == null) {
            REGISTERED_PLAYERS.remove(player.getUUID());
            return;
        }

        playerPatch.getEventListener().addEventListener(
                PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT,
                ANIMATION_BEGIN_LISTENER,
                (AnimationBeginEvent animationEvent) -> onAttackAnimationBegin(player, animationEvent)
        );
    }

    private static void onAttackAnimationBegin(ServerPlayer player, AnimationBeginEvent event) {
        if (!(event.getAnimation() instanceof AttackAnimation)) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        ArclightConduitBalance.Profile conduit = ArclightConduitBalance.resolve(weapon);
        if (conduit != null) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            float currentMana = magicData.getMana();
            if (currentMana >= conduit.manaCost()) {
                magicData.setMana(currentMana - conduit.manaCost());
                EMPOWERED_ATTACKS.put(player.getUUID(),
                        player.level().getGameTime() + EMPOWERED_ATTACK_WINDOW_TICKS);
            } else {
                EMPOWERED_ATTACKS.remove(player.getUUID());
            }
            return;
        }

        EMPOWERED_ATTACKS.remove(player.getUUID());
        EvergateUnityBalance.Profile unity = EvergateUnityBalance.resolve(weapon);
        if (unity != null) {
            player.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(),
                    unity.stunImmunityDurationTicks(), 0, false, true, true));
        }
    }

    private static void refreshFormAttributes(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        ArclightConduitBalance.Profile conduit = ArclightConduitBalance.resolve(weapon);
        EvergateUnityBalance.Profile unity = EvergateUnityBalance.resolve(weapon);

        refreshModifier(player, AttributeRegistry.MANA_REGEN.get(), MANA_REGEN_MODIFIER,
                MANA_REGEN_MODIFIER_NAME, conduit == null ? 0.0D : conduit.manaRegenerationBonus(),
                AttributeModifier.Operation.MULTIPLY_BASE);
        refreshModifier(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER,
                MOVEMENT_SPEED_MODIFIER_NAME, unity == null ? 0.0D : unity.movementSpeedBonus(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void refreshModifier(ServerPlayer player, Attribute attribute, UUID id, String name,
                                        double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier current = instance.getModifier(id);
        if (amount == 0.0D) {
            if (current != null) {
                instance.removeModifier(current);
            }
            return;
        }

        if (current != null && Double.compare(current.getAmount(), amount) == 0
                && current.getOperation() == operation) {
            return;
        }

        if (current != null) {
            instance.removeModifier(current);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static void dealHolyDamage(ServerPlayer attacker, LivingEntity target, float amount) {
        if (amount <= 0.0F || !target.isAlive()) {
            return;
        }

        UUID targetId = target.getUUID();
        Set<UUID> processing = PROCESSING_HOLY_DAMAGE.get();
        processing.add(targetId);
        Vec3 movement = target.getDeltaMovement();
        int invulnerableTime = target.invulnerableTime;
        try {
            target.invulnerableTime = 0;
            DamageSource source = new DamageSource(
                    attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(ISSDamageTypes.HOLY_MAGIC),
                    attacker,
                    attacker
            );
            target.hurt(source, amount);
        } finally {
            target.invulnerableTime = invulnerableTime;
            target.setDeltaMovement(movement);
            target.hurtMarked = true;
            processing.remove(targetId);
        }
    }

    private static void absorbBarrierDamage(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || event.getAmount() <= 0.0F) {
            return;
        }

        BarrierState barrier = BARRIERS.get(player.getUUID());
        if (barrier == null || barrier.amount <= 0.0F || player.level().getGameTime() >= barrier.expiresAt) {
            return;
        }

        float absorbed = Math.min(barrier.amount, event.getAmount());
        barrier.amount -= absorbed;
        event.setAmount(Math.max(0.0F, event.getAmount() - absorbed));
        if (barrier.amount <= 0.001F) {
            clearBarrier(player);
        } else {
            syncBarrier(player, barrier.amount);
        }
    }
    public static void grantBarrier(ServerPlayer player, float amount, int durationTicks) {
        addBarrier(player, amount, durationTicks);
    }

    private static void addBarrier(ServerPlayer player, float amount, int durationTicks) {
        if (amount <= 0.0F || durationTicks <= 0) {
            return;
        }

        BarrierState barrier = BARRIERS.computeIfAbsent(player.getUUID(), ignored -> new BarrierState());
        barrier.amount = Math.min(player.getMaxHealth(), barrier.amount + amount);
        barrier.expiresAt = player.level().getGameTime() + durationTicks;
        syncBarrier(player, barrier.amount);
    }

    private static void tickBarrier(ServerPlayer player) {
        BarrierState barrier = BARRIERS.get(player.getUUID());
        if (barrier == null) {
            return;
        }

        if (player.level().getGameTime() >= barrier.expiresAt) {
            clearBarrier(player);
            return;
        }

        float cappedAmount = Math.min(barrier.amount, player.getMaxHealth());
        if (Float.compare(cappedAmount, barrier.amount) != 0) {
            barrier.amount = cappedAmount;
            syncBarrier(player, barrier.amount);
        }
    }

    private static void clearBarrier(ServerPlayer player) {
        if (BARRIERS.remove(player.getUUID()) != null) {
            syncBarrier(player, 0.0F);
        }
    }

    private static void syncBarrier(ServerPlayer player, float amount) {
        PBNetworkManager.sendToTrackingAndSelf(player, new SPBarrierSync(player.getId(), amount));
    }

    private static void cleanupPlayer(net.minecraft.world.entity.player.Player player) {
        UUID playerId = player.getUUID();
        REGISTERED_PLAYERS.remove(playerId);
        EMPOWERED_ATTACKS.remove(playerId);
        boolean hadBarrier = BARRIERS.remove(playerId) != null;
        if (hadBarrier && player instanceof ServerPlayer serverPlayer) {
            syncBarrier(serverPlayer, 0.0F);
        }

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch != null) {
            playerPatch.getEventListener().removeListener(
                    PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT, ANIMATION_BEGIN_LISTENER);
        }

        AttributeInstance manaRegen = player.getAttribute(AttributeRegistry.MANA_REGEN.get());
        if (manaRegen != null) {
            manaRegen.removeModifier(MANA_REGEN_MODIFIER);
        }
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) {
            movement.removeModifier(MOVEMENT_SPEED_MODIFIER);
        }
    }

    private static final class BarrierState {
        private float amount;
        private long expiresAt;
    }
}