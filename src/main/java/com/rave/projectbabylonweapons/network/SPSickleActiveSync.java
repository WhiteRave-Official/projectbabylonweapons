package com.rave.projectbabylonweapons.network;

import com.rave.projectbabylonweapons.client.input.SickleInputState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SPSickleActiveSync {
    private final UUID playerId;
    private final int entityId;

    public SPSickleActiveSync(UUID playerId, int entityId) {
        this.playerId = playerId;
        this.entityId = entityId;
    }

    public SPSickleActiveSync(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> SickleInputState.setProjectile(playerId, entityId));
        context.setPacketHandled(true);
    }
}
