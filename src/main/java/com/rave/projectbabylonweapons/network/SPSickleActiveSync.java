package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SPSickleActiveSync(UUID playerId, int entityId) implements CustomPacketPayload {

    public static final Type<SPSickleActiveSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "sickle_active_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPSickleActiveSync> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SPSickleActiveSync::playerId,
                    ByteBufCodecs.INT,     SPSickleActiveSync::entityId,
                    SPSickleActiveSync::new
            );

    @Override
    public Type<SPSickleActiveSync> type() {
        return TYPE;
    }

    public static void handle(SPSickleActiveSync packet, IPayloadContext context) {
        context.enqueueWork(() -> SickleThrowSkill.setClientActiveProjectile(packet.playerId(), packet.entityId()));
    }
}
