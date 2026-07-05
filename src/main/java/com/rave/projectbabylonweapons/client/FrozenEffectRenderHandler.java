package com.rave.projectbabylonweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.block.renderer.FrozenDebuffIceBlockDisplayItemRenderer;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.init.PBModItems;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class FrozenEffectRenderHandler {
    private static final float MODEL_SCALE = 2.0F;
    private static final double Y_OFFSET = -1.0D;
    private static final long SYNC_TTL_MS = 300L;
    private static FrozenDebuffIceBlockDisplayItemRenderer frozenRenderer = new FrozenDebuffIceBlockDisplayItemRenderer();
    private static final Map<Integer, Long> CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS = new ConcurrentHashMap<>();
    private static ClientLevel lastLoggedLevel;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level == null) {
            return;
        }

        long now = Util.getMillis();
        CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS.entrySet().removeIf(entry -> entry.getValue() <= now);

        if (lastLoggedLevel != level) {
            frozenRenderer = new FrozenDebuffIceBlockDisplayItemRenderer();
            lastLoggedLevel = level;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        ItemStack frozenBlockStack = new ItemStack(PBModItems.FROZEN_DEBUFF_ICE_BLOCK.get());
        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean renderedAny = false;

        for (Entity rawEntity : level.entitiesForRendering()) {
            if (!(rawEntity instanceof LivingEntity entity)) {
                continue;
            }

            boolean hasFrozen = entity.hasEffect(PBMEffects.FROZEN);
            boolean syncedFrozen = CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS.getOrDefault(entity.getId(), 0L) > now;
            boolean shouldRenderFrozen = hasFrozen || syncedFrozen;
            boolean isAlive = entity.isAlive();

            if (!shouldRenderFrozen || !isAlive) {
                continue;
            }

            double renderX = Mth.lerp(partialTick, entity.xOld, entity.getX()) - camX - 1.0D;
            double renderY = Mth.lerp(partialTick, entity.yOld, entity.getY()) - camY + Y_OFFSET;
            double renderZ = Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camZ - 1.0D;

            poseStack.pushPose();
            poseStack.translate(renderX, renderY, renderZ);
            poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

            frozenRenderer.renderByItem(
                    frozenBlockStack,
                    ItemDisplayContext.GROUND,
                    poseStack,
                    bufferSource,
                    LevelRenderer.getLightColor(entity.level(), entity.blockPosition()),
                    0
            );

            poseStack.popPose();
            renderedAny = true;
        }

        if (renderedAny) {
            bufferSource.endBatch();
        }
    }

    public static void updateFrozenVisualState(int entityId, boolean frozen) {
        if (frozen) {
            CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS.put(entityId, Util.getMillis() + SYNC_TTL_MS);
        } else {
            CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CLIENT_FROZEN_ENTITY_SYNC_UNTIL_MS.clear();
        lastLoggedLevel = null;
    }
}

