package com.rave.projectbabylonweapons.passive.dragonsteel;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.setbonus.ArmorSetDefinition;
import com.rave.projectbabylonmaterials.setbonus.ArmorSetRegistry;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import com.rave.projectbabylonweapons.world.entity.projectile.DragonsteelWyrmEchoProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DragonsteelWyrmEchoPassive {
    private static final String COUNTER_TAG = "project_babylon_weapons.dragonsteel_wyrm_echo_hits";
    private static final String REBIRTH_COOLDOWN_UNTIL_TAG = "project_babylon_armors.rebirth_cooldown_until";
    private static final UUID SKILL_CAST_LISTENER = UUID.fromString("2f10c82d-a414-4df4-b626-b8700b190f73");
    private static final Map<UUID, Integer> REGISTERED_PATCH_IDENTITIES = new ConcurrentHashMap<>();
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.dragonsteel.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/dragonsteel_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/dragonsteel_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.dragonsteel.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.dragonsteel.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("dragonsteel")
    );

    private DragonsteelWyrmEchoPassive() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide) {
            return;
        }

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(event.player, PlayerPatch.class);
        if (playerPatch == null) {
            return;
        }

        int patchIdentity = System.identityHashCode(playerPatch.getEventListener());
        Integer previousIdentity = REGISTERED_PATCH_IDENTITIES.put(event.player.getUUID(), patchIdentity);
        if (previousIdentity == null || previousIdentity != patchIdentity) {
            playerPatch.getEventListener().addEventListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    SKILL_CAST_LISTENER,
                    skillCastEvent -> onSkillCast(event.player, skillCastEvent)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        REGISTERED_PATCH_IDENTITIES.remove(event.getEntity().getUUID());

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);
        if (playerPatch != null) {
            playerPatch.getEventListener().removeListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    SKILL_CAST_LISTENER
            );
        }
    }

    private static void onSkillCast(Player player, SkillCastEvent event) {
        if (player.level().isClientSide || event.isCanceled() || !event.isExecutable() || event.getSkillContainer() == null) {
            return;
        }
        if (event.getSkillContainer().getSlot() != SkillSlots.BASIC_ATTACK) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        DragonsteelWyrmEchoBalance.Profile profile = DragonsteelWyrmEchoBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        boolean berserk = isBerserkMode(player);
        int interval = profile.attackInterval(berserk);
        CompoundTag data = player.getPersistentData();
        int hits = data.getInt(COUNTER_TAG) + 1;
        if (hits < interval) {
            data.putInt(COUNTER_TAG, hits);
            return;
        }

        data.putInt(COUNTER_TAG, 0);
        spawnEcho(player, weapon, profile, berserk);
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    private static void spawnEcho(LivingEntity attacker, ItemStack weapon, DragonsteelWyrmEchoBalance.Profile profile, boolean berserk) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 direction = attacker.getLookAngle();
        Vec3 flatDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (flatDirection.lengthSqr() < 1.0E-6D) {
            flatDirection = Vec3.directionFromRotation(0.0F, attacker.getYRot());
            flatDirection = new Vec3(flatDirection.x, 0.0D, flatDirection.z);
        }
        flatDirection = flatDirection.normalize();

        float weaponDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float echoDamage = weaponDamage * profile.damageMultiplier(berserk);
        float trailDamage = weaponDamage * profile.trailDamageMultiplier(berserk);
        if (echoDamage <= 0.0F && trailDamage <= 0.0F) {
            return;
        }

        double spawnY = attacker.getY() + attacker.getBbHeight() * 0.55D;
        Vec3 spawnPos = attacker.position().add(0.0D, attacker.getBbHeight() * 0.55D, 0.0D).add(flatDirection.scale(0.75D));
        DragonsteelWyrmEchoProjectileEntity projectile = new DragonsteelWyrmEchoProjectileEntity(serverLevel);
        projectile.setPos(spawnPos.x, spawnY, spawnPos.z);
        projectile.configure(attacker, weapon, echoDamage, trailDamage, profile.rangeBlocks(berserk), profile.projectileSpeed(), profile.hitRadius(), profile.trailLifetimeTicks(berserk), profile.trailDamageIntervalTicks(), berserk);
        projectile.shoot(flatDirection.x, 0.0D, flatDirection.z, profile.projectileSpeed(), 0.0F);
        serverLevel.addFreshEntity(projectile);
        playEchoLaunchSound(attacker);
    }

    private static void playEchoLaunchSound(LivingEntity attacker) {
        SoundEvent sound = MagicProjectileStaffWeapon.getIronsSpellbooksSound("cast.generic.evocation");
        if (sound == null) {
            return;
        }

        attacker.level().playSound(
                null,
                attacker.getX(),
                attacker.getY(),
                attacker.getZ(),
                sound,
                SoundSource.PLAYERS,
                0.45F,
                1.12F
        );
    }

    private static boolean isBerserkMode(LivingEntity entity) {
        ArmorSetDefinition activeSet = ArmorSetRegistry.findMatching(entity);
        if (activeSet == null || !activeSet.getId().startsWith("dragonsteel_")) {
            return false;
        }
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        long cooldownUntil = entity.getPersistentData().getLong(REBIRTH_COOLDOWN_UNTIL_TAG);
        return cooldownUntil > serverLevel.getGameTime();
    }
}
