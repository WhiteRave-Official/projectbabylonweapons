package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.WeaponVisualEffectClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SPPlayWeaponVisualEffect(String effectId, int entityId) implements CustomPacketPayload {

    public static final Type<SPPlayWeaponVisualEffect> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "play_weapon_visual_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPPlayWeaponVisualEffect> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SPPlayWeaponVisualEffect::effectId,
                    ByteBufCodecs.VAR_INT,     SPPlayWeaponVisualEffect::entityId,
                    SPPlayWeaponVisualEffect::new
            );

    @Override
    public Type<SPPlayWeaponVisualEffect> type() {
        return TYPE;
    }

    public static void handle(SPPlayWeaponVisualEffect packet, IPayloadContext context) {
        context.enqueueWork(() -> WeaponVisualEffectClientHelper.play(packet.effectId(), packet.entityId()));
    }
}
