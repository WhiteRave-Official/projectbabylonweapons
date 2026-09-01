package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.client.BarrierClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SPBarrierSync {
    private final int entityId;
    private final float amount;

    public SPBarrierSync(int entityId, float amount) {
        this.entityId = entityId;
        this.amount = amount;
    }

    public SPBarrierSync(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.amount = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeFloat(this.amount);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> BarrierClientState.update(this.entityId, this.amount));
        context.setPacketHandled(true);
    }
}