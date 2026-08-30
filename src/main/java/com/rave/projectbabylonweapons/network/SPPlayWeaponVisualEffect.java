package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.client.WeaponVisualEffectClientHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SPPlayWeaponVisualEffect {
    private final String effectId;
    private final int entityId;

    public SPPlayWeaponVisualEffect(String effectId, int entityId) {
        this.effectId = effectId;
        this.entityId = entityId;
    }

    public SPPlayWeaponVisualEffect(FriendlyByteBuf buf) {
        this.effectId = buf.readUtf();
        this.entityId = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.effectId);
        buf.writeVarInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            WeaponVisualEffectClientHelper.play(this.effectId, this.entityId);
        });
        context.setPacketHandled(true);
    }
}