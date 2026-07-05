package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public record CPPullOwnerToTarget() implements CustomPacketPayload {

    public static final Type<CPPullOwnerToTarget> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "pull_owner_to_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CPPullOwnerToTarget> STREAM_CODEC =
            StreamCodec.unit(new CPPullOwnerToTarget());

    @Override
    public Type<CPPullOwnerToTarget> type() {
        return TYPE;
    }

    public static void handle(CPPullOwnerToTarget packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
            if (playerPatch == null) return;

            SickleProjectileEntity projectile = SickleThrowSkill.getActiveProjectilePublic(player);
            if (projectile == null || !projectile.isTethered()) return;

            playerPatch.playAnimationSynchronized(PBAnimations.SICKLE_PULLING, 0.0f);
            projectile.pullOwnerToTarget();
        });
    }
}
