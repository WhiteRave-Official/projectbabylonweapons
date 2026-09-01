package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PBNetworkManager {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {

        CHANNEL.registerMessage(
                id++,
                CPPullOwnerToTarget.class,
                CPPullOwnerToTarget::encode,
                CPPullOwnerToTarget::new,
                CPPullOwnerToTarget::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );


        CHANNEL.registerMessage(
                id++,
                CPPullTargetToOwner.class,
                CPPullTargetToOwner::encode,
                CPPullTargetToOwner::new,
                CPPullTargetToOwner::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                id++,
                SPFrozenVisualSync.class,
                SPFrozenVisualSync::encode,
                SPFrozenVisualSync::new,
                SPFrozenVisualSync::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                id++,
                SPSickleActiveSync.class,
                SPSickleActiveSync::encode,
                SPSickleActiveSync::new,
                SPSickleActiveSync::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                id++,
                SPPlayWeaponVisualEffect.class,
                SPPlayWeaponVisualEffect::encode,
                SPPlayWeaponVisualEffect::new,
                SPPlayWeaponVisualEffect::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id++,
                SPBarrierSync.class,
                SPBarrierSync::encode,
                SPBarrierSync::new,
                SPBarrierSync::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToTrackingAndSelf(Entity entity, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }
}
