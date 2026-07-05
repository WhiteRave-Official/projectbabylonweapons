package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.FrozenEffectRenderHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SPFrozenVisualSync(int entityId, boolean frozen) implements CustomPacketPayload {

    public static final Type<SPFrozenVisualSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "frozen_visual_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPFrozenVisualSync> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SPFrozenVisualSync::entityId,
                    ByteBufCodecs.BOOL,    SPFrozenVisualSync::frozen,
                    SPFrozenVisualSync::new
            );

    @Override
    public Type<SPFrozenVisualSync> type() {
        return TYPE;
    }

    public static void handle(SPFrozenVisualSync packet, IPayloadContext context) {
        context.enqueueWork(() -> FrozenEffectRenderHandler.updateFrozenVisualState(packet.entityId(), packet.frozen()));
    }
}
