package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PBNetworkManager {

    private static final String PROTOCOL_VERSION = "1";

    private PBNetworkManager() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ProjectBabylonWeapons.MODID).versioned(PROTOCOL_VERSION);

        registrar.playToServer(CPPullOwnerToTarget.TYPE,      CPPullOwnerToTarget.STREAM_CODEC,      CPPullOwnerToTarget::handle);
        registrar.playToServer(CPPullTargetToOwner.TYPE,      CPPullTargetToOwner.STREAM_CODEC,      CPPullTargetToOwner::handle);
        registrar.playToClient(SPFrozenVisualSync.TYPE,       SPFrozenVisualSync.STREAM_CODEC,       SPFrozenVisualSync::handle);
        registrar.playToClient(SPSickleActiveSync.TYPE,       SPSickleActiveSync.STREAM_CODEC,       SPSickleActiveSync::handle);
        registrar.playToClient(SPPlayWeaponVisualEffect.TYPE, SPPlayWeaponVisualEffect.STREAM_CODEC, SPPlayWeaponVisualEffect::handle);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }
}
