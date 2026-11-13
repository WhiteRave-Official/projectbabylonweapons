package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowChains;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.function.Supplier;

public class CPPullTargetToOwner {
    private final int projectileId;

    public CPPullTargetToOwner(int projectileId) { this.projectileId = projectileId; }

    public CPPullTargetToOwner(FriendlyByteBuf buf) { this.projectileId = buf.readVarInt(); }

    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(projectileId); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
            if (playerPatch == null) return;

            var projectiles = SickleThrowChains.projectiles(player).stream().filter(SickleProjectileEntity::isTethered).toList();
            if (projectiles.stream().noneMatch(projectile -> projectile.getId() == projectileId)) return;


            playerPatch.playAnimationSynchronized(PBAnimations.SICKLE_HOOKING, 0.0f);
            for (var projectile : projectiles) projectile.pullTargetToOwner();
        });
        context.setPacketHandled(true);
    }
}
