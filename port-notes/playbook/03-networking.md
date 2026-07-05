# Networking Porting Playbook: Forge 1.20.1 -> NeoForge 1.21.1

Domain: the packet/channel system. Forge `SimpleChannel` + `NetworkEvent.Context` ->
NeoForge `PayloadRegistrar` (via `RegisterPayloadHandlersEvent`) + records implementing
`CustomPacketPayload` with a `StreamCodec` + `IPayloadContext`.

Everything below is VERIFIED against
`build/moddev/artifacts/neoforge-21.1.233-merged.jar` (javap) and matched to the author's
GOLD reference commit `4b04b55 Port networking to NeoForge 1.21.1 packet channel API`
(PrjBabilonMaterialPort, `git diff main neo_1.21.1 -- '*network*'`).

## Weapons files in scope (network/)
5 packet classes + 1 manager:
- `CPPullOwnerToTarget.java`   -- **serverbound**, EMPTY (no fields)
- `CPPullTargetToOwner.java`   -- **serverbound**, EMPTY (no fields)
- `SPFrozenVisualSync.java`    -- **clientbound**, fields: `int entityId` (varInt), `boolean frozen`
- `SPPlayWeaponVisualEffect.java` -- **clientbound**, fields: `String effectId`, `int entityId` (varInt)
- `SPSickleActiveSync.java`    -- **clientbound**, fields: `UUID playerId`, `int entityId` (writeInt, NOT varInt)
- `PBNetworkManager.java`      -- channel + register() + send helpers

Naming convention in this mod: `CP*` = client->server (serverbound), `SP*` = server->client
(clientbound). Determine direction from the original
`registerMessage(... NetworkDirection.PLAY_TO_SERVER/PLAY_TO_CLIENT)` and from the send call
site, NOT by guessing from the prefix.

Call sites that send packets (these do NOT need to change if you keep the manager helpers --
see "Keep the send helpers" recipe):
- `client/ClientSetup.java:39,44` -> `PBNetworkManager.sendToServer(new CPPull...())`
- `handler/FrozenEffectHandler.java` (many) -> `sendToTrackingAndSelf` / `sendToPlayer`
- `handler/WeaponVisualEffectHelper.java:87` -> `sendToTrackingAndSelf`
- `skill/weapon_innate/SickleThrowSkill.java:254,261` -> `sendToPlayer`
- `ProjectBabylonWeapons.java:71` -> `PBNetworkManager.register()` (call site MOVES, see gotcha)

---

## 1. Import remap table

| Forge import (from) | NeoForge import (to) | Kind |
|---|---|---|
| `net.minecraftforge.network.NetworkRegistry` | `net.neoforged.neoforge.network.registration.PayloadRegistrar` | SEMANTIC |
| `net.minecraftforge.network.simple.SimpleChannel` | `net.neoforged.neoforge.network.registration.PayloadRegistrar` | SEMANTIC |
| `net.minecraftforge.network.NetworkEvent` | `net.neoforged.neoforge.network.handling.IPayloadContext` | SEMANTIC |
| `net.minecraftforge.network.PacketDistributor` | `net.neoforged.neoforge.network.PacketDistributor` | SEMANTIC (same simple name, methods differ) |
| `net.minecraft.network.FriendlyByteBuf` | `net.minecraft.network.RegistryFriendlyByteBuf` | SEMANTIC (only inside packet codecs) |

**REMOVED imports (no replacement; delete the line):**
- `net.minecraftforge.network.NetworkDirection` -- direction is now encoded by choosing
  `registrar.playToServer` vs `registrar.playToClient`.
- `java.util.function.Supplier` -- the `Supplier<NetworkEvent.Context>` handler param is gone.

**NEW imports to ADD to each ported packet class:**
- `net.minecraft.network.RegistryFriendlyByteBuf`
- `net.minecraft.network.codec.StreamCodec`
- `net.minecraft.network.codec.ByteBufCodecs`        (only if it has primitive/String fields)
- `net.minecraft.network.protocol.common.custom.CustomPacketPayload`
- `net.minecraft.resources.ResourceLocation`
- `net.neoforged.neoforge.network.handling.IPayloadContext`
- `com.rave.projectbabylonweapons.ProjectBabylonWeapons`  (for `MODID` in the TYPE id)
- `net.minecraft.core.UUIDUtil`   (ONLY for `SPSickleActiveSync`, which has a UUID field)

**NEW imports for the manager (`PBNetworkManager`):**
- `net.minecraft.network.protocol.common.custom.CustomPacketPayload`
- `net.neoforged.neoforge.network.PacketDistributor`
- `net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent`
- `net.neoforged.neoforge.network.registration.PayloadRegistrar`
- keep `net.minecraft.server.level.ServerPlayer`, `net.minecraft.world.entity.Entity`

---

## 2. Verified target API signatures (from merged.jar)

```
// net.minecraft.network.protocol.common.custom.CustomPacketPayload
Type<? extends CustomPacketPayload> type();          // instance method you override
// nested record:  CustomPacketPayload$Type<T>(ResourceLocation id)   -> new Type<>(rl)

// net.minecraft.network.codec.StreamCodec  (Mojang)
static <B,V> StreamCodec<B,V> unit(V value);                                  // EMPTY payloads
static <B,T1,T2,...> StreamCodec<B,T> composite(codec1, getter1, ..., ctor); // 1..6 components

// net.minecraft.network.codec.ByteBufCodecs  (field codecs; all StreamCodec<ByteBuf,X>)
BOOL, INT, VAR_INT, FLOAT, DOUBLE, STRING_UTF8
// net.minecraft.core.UUIDUtil.STREAM_CODEC : StreamCodec<ByteBuf, UUID>

// net.neoforged.neoforge.network.handling.IPayloadContext
Player player();                                  // NOTE: returns Player, not ServerPlayer
CompletableFuture<Void> enqueueWork(Runnable);
Connection reply(CustomPacketPayload);
PacketFlow flow();

// net.neoforged.neoforge.network.registration.PayloadRegistrar
PayloadRegistrar versioned(String version);
<T extends CustomPacketPayload> PayloadRegistrar playToServer(Type<T>, StreamCodec<? super RegistryFriendlyByteBuf,T>, IPayloadHandler<T>);
<T extends CustomPacketPayload> PayloadRegistrar playToClient(Type<T>, StreamCodec<? super RegistryFriendlyByteBuf,T>, IPayloadHandler<T>);
// IPayloadHandler<T> == void handle(T payload, IPayloadContext context)  -> method ref `Packet::handle`

// net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
PayloadRegistrar registrar(String namespace);

// net.neoforged.neoforge.network.PacketDistributor  (STATIC methods now)
static void sendToServer(CustomPacketPayload, CustomPacketPayload...);
static void sendToPlayer(ServerPlayer, CustomPacketPayload, CustomPacketPayload...);
static void sendToPlayersTrackingEntityAndSelf(Entity, CustomPacketPayload, CustomPacketPayload...);
static void sendToPlayersNear(ServerLevel, ServerPlayer exclude, double x,y,z, double radius, CustomPacketPayload, ...);
```

### Field-type -> codec mapping (build the `StreamCodec.composite` from these)
| Original buffer call | NeoForge codec |
|---|---|
| `writeDouble`/`readDouble` | `ByteBufCodecs.DOUBLE` |
| `writeVarInt`/`readVarInt` | `ByteBufCodecs.VAR_INT` |
| `writeInt`/`readInt`       | `ByteBufCodecs.INT` |
| `writeBoolean`/`readBoolean` | `ByteBufCodecs.BOOL` |
| `writeFloat`/`readFloat`   | `ByteBufCodecs.FLOAT` |
| `writeUtf`/`readUtf`       | `ByteBufCodecs.STRING_UTF8` |
| `writeUUID`/`readUUID`     | `UUIDUtil.STREAM_CODEC` |

Read the ORIGINAL `decode`/buf-constructor to learn which write call each field used
(e.g. `SPSickleActiveSync` uses `writeInt` -> `ByteBufCodecs.INT`, while `SPFrozenVisualSync`
uses `writeVarInt` -> `ByteBufCodecs.VAR_INT`). Do not blindly pick VAR_INT for every int.

---

## 3. Recipe A -- clientbound packet WITH fields (the common case)

This is the author's exact pattern (from `ClientboundCritEffectPacket` /
`ClientboundDragonsteelCooldownPacket`). Applied here to `SPFrozenVisualSync`.

BEFORE (Forge):
```java
public class SPFrozenVisualSync {
    private final int entityId;
    private final boolean frozen;

    public SPFrozenVisualSync(int entityId, boolean frozen) { this.entityId = entityId; this.frozen = frozen; }
    public SPFrozenVisualSync(FriendlyByteBuf buf) { this.entityId = buf.readVarInt(); this.frozen = buf.readBoolean(); }
    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(this.entityId); buf.writeBoolean(this.frozen); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> FrozenEffectRenderHandler.updateFrozenVisualState(this.entityId, this.frozen));
        context.setPacketHandled(true);
    }
}
```

AFTER (NeoForge):
```java
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
```

Mechanical steps:
1. `class` -> `record` with the old fields as the record header components, in field order.
   Delete the manual fields, the all-args constructor, the `(FriendlyByteBuf)` constructor,
   and `encode`. `implements CustomPacketPayload`.
2. Add `TYPE`: `new Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "<snake_name>"))`.
   Pick a stable unique snake_case path per packet (it is the wire id; keep it constant).
3. Add `STREAM_CODEC` via `StreamCodec.composite(codec, Rec::getter, ..., Rec::new)`,
   using the field-type table. Constructor `Rec::new` is ALWAYS the last arg.
4. `@Override public Type<Rec> type() { return TYPE; }`
5. Convert `handle(Supplier<NetworkEvent.Context>)` to
   `public static void handle(Rec packet, IPayloadContext context)`. Inside: keep the
   `context.enqueueWork(...)` body but change `this.field` -> `packet.field()` (record accessor).
   DELETE `contextSupplier.get()` and `context.setPacketHandled(true)` (both gone in NeoForge).

Other clientbound packets follow identically:
- `SPPlayWeaponVisualEffect(String effectId, int entityId)` ->
  `composite(ByteBufCodecs.STRING_UTF8, ::effectId, ByteBufCodecs.VAR_INT, ::entityId, ::new)`,
  body `WeaponVisualEffectClientHelper.play(packet.effectId(), packet.entityId())`.
  id e.g. `"play_weapon_visual_effect"`.
- `SPSickleActiveSync(UUID playerId, int entityId)` ->
  `composite(UUIDUtil.STREAM_CODEC, ::playerId, ByteBufCodecs.INT, ::entityId, ::new)`
  (NOTE: `ByteBufCodecs.INT`, because the original used `writeInt`, not `writeVarInt`).
  Add `import net.minecraft.core.UUIDUtil;`. Body
  `SickleThrowSkill.setClientActiveProjectile(packet.playerId(), packet.entityId())`.
  id e.g. `"sickle_active_sync"`.

---

## 4. Recipe B -- EMPTY packet (no fields) -- serverbound

`CPPullOwnerToTarget` / `CPPullTargetToOwner` carry no data. The author had no empty-packet
example, so this is derived but fully jar-verified: use `StreamCodec.unit(singleton)`.
These are serverbound, so the handler needs the sender as a `ServerPlayer`.

BEFORE (Forge):
```java
public class CPPullOwnerToTarget {
    public CPPullOwnerToTarget() {}
    public CPPullOwnerToTarget(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();        // <-- sender
            if (player == null) return;
            ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
            if (playerPatch == null) return;
            SickleProjectileEntity projectile = SickleThrowSkill.getActiveProjectilePublic(player);
            if (projectile == null || !projectile.isTethered()) return;
            playerPatch.playAnimationSynchronized(PBAnimations.SICKLE_PULLING, 0.0f);
            projectile.pullOwnerToTarget();
        });
        context.setPacketHandled(true);
    }
}
```

AFTER (NeoForge):
```java
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
            if (!(context.player() instanceof ServerPlayer player)) return;   // sender, server side

            ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
            if (playerPatch == null) return;

            SickleProjectileEntity projectile = SickleThrowSkill.getActiveProjectilePublic(player);
            if (projectile == null || !projectile.isTethered()) return;

            playerPatch.playAnimationSynchronized(PBAnimations.SICKLE_PULLING, 0.0f);
            projectile.pullOwnerToTarget();
        });
    }
}
```

Empty-packet specifics:
- `public record Name() implements CustomPacketPayload {` -- zero-arg record.
- `STREAM_CODEC = StreamCodec.unit(new Name())` -- writes nothing, decodes to a fresh instance.
- `context.getSender()` (Forge) -> server-side sender via `context.player()` which returns the
  generic `Player`; pattern-match/cast to `ServerPlayer`:
  `if (!(context.player() instanceof ServerPlayer player)) return;`
  (`context.player()` is the connection's player; on a serverbound handler it IS the sender.)
- `CPPullTargetToOwner` is identical with id `"pull_target_to_owner"`, animation
  `PBAnimations.SICKLE_HOOKING`, and `projectile.pullTargetToOwner()`.

---

## 5. Recipe C -- PBNetworkManager (channel + register + send helpers)

KEEP the three send helper methods so NONE of the ~16 call sites change. Just retype the
parameter `Object` -> `CustomPacketPayload` and swap the bodies to static `PacketDistributor`.

BEFORE (Forge):
```java
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
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int packetId;

    public static void register() {
        CHANNEL.registerMessage(packetId++, CPPullOwnerToTarget.class, CPPullOwnerToTarget::encode,
                CPPullOwnerToTarget::new, CPPullOwnerToTarget::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        // ... 4 more registerMessage(...) calls
    }

    public static void sendToServer(Object packet) { CHANNEL.sendToServer(packet); }
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    public static void sendToTrackingAndSelf(Entity entity, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }
}
```

AFTER (NeoForge):
```java
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

        registrar.playToServer(CPPullOwnerToTarget.TYPE,   CPPullOwnerToTarget.STREAM_CODEC,   CPPullOwnerToTarget::handle);
        registrar.playToServer(CPPullTargetToOwner.TYPE,   CPPullTargetToOwner.STREAM_CODEC,   CPPullTargetToOwner::handle);
        registrar.playToClient(SPFrozenVisualSync.TYPE,    SPFrozenVisualSync.STREAM_CODEC,    SPFrozenVisualSync::handle);
        registrar.playToClient(SPSickleActiveSync.TYPE,    SPSickleActiveSync.STREAM_CODEC,    SPSickleActiveSync::handle);
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
```

Mapping of the three send helpers (Forge `PacketDistributor.X.with(supplier)` -> NeoForge static):
| Forge | NeoForge |
|---|---|
| `CHANNEL.sendToServer(p)` | `PacketDistributor.sendToServer(p)` |
| `CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p)` | `PacketDistributor.sendToPlayer(player, p)` |
| `CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), p)` | `PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, p)` |
| (if present) `PacketDistributor.NEAR.with(() -> new TargetPoint(...))` | `PacketDistributor.sendToPlayersNear(level, exclude, x, y, z, radius, p)` |

Delete `packetId`, `CHANNEL`, `SimpleChannel`, `NetworkRegistry`, `NetworkDirection`, `Optional`.
`registerMessage(id, class, encode, decode, handle, Optional.of(dir))` collapses to a single
`registrar.playToServer/playToClient(TYPE, STREAM_CODEC, ::handle)` line per packet -- direction
chosen by the method name (`PLAY_TO_SERVER` -> `playToServer`, `PLAY_TO_CLIENT` -> `playToClient`).

---

## 6. Recipe D -- wiring register() onto the mod bus (entry-point change)

This lives in `ProjectBabylonWeapons.java` (mod entry-point domain), but it is REQUIRED for
networking to function and is a frequent miss. The Forge code calls `register()` inside
`commonSetup` (FMLCommonSetupEvent). In NeoForge, payload registration MUST happen during
`RegisterPayloadHandlersEvent` on the MOD bus.

BEFORE (Forge, in `commonSetup`):
```java
private void commonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
        PBNetworkManager.register();          // <-- REMOVE from here
        ...
    });
}
```

AFTER (NeoForge, in the constructor next to the other `modBus.register/addListener` calls):
```java
modBus.addListener(PBNetworkManager::register);   // RegisterPayloadHandlersEvent listener
```
Matches the gold reference exactly: `modBus.addListener(PBNetwork::register);`
(PrjBabilonMaterials `ProjectBabylonMaterials` constructor).

---

## 7. Gotchas / differs from a naive port

1. **Registration moves bus + phase.** Not in `commonSetup` anymore -- it must be a
   `RegisterPayloadHandlersEvent` listener on the mod bus (`modBus.addListener(PBNetworkManager::register)`).
   `register()` gains a `RegisterPayloadHandlersEvent` parameter.
2. **`context.setPacketHandled(true)` is DELETED.** There is no equivalent; do not try to keep it.
3. **`contextSupplier.get()` is DELETED.** The handler receives `IPayloadContext` directly as the
   2nd param. Handler shape becomes `static void handle(PacketType packet, IPayloadContext ctx)`.
4. **`context.getSender()` -> `(ServerPlayer) context.player()`.** `IPayloadContext.player()`
   returns the generic `Player`; cast/pattern-match to `ServerPlayer` for serverbound handlers.
   Keep the null-guard via `instanceof`.
5. **`this.field` -> `packet.field()`.** Once the class is a record and the handler is static,
   you must access data through the `packet` argument's record accessors, not `this`.
6. **`writeInt` != `writeVarInt`.** `SPSickleActiveSync` used `writeInt` -> map to
   `ByteBufCodecs.INT`. Using `VAR_INT` would silently change the wire format. Read the original
   buffer code for each field.
7. **UUID has no `ByteBufCodecs` constant.** Use `net.minecraft.core.UUIDUtil.STREAM_CODEC`.
8. **Empty payloads need `StreamCodec.unit(new Packet())`,** NOT `composite`. `composite` requires
   at least one component. The unit value is a shared singleton instance -- fine for stateless packets.
9. **`StreamCodec.composite` always ends with the constructor ref** (`Packet::new`) and pairs each
   codec with its accessor (`Packet::field`) in declaration order. Max 6 components; if a packet
   has >6 fields you must nest/compose (none here do).
10. **`StreamCodec<RegistryFriendlyByteBuf, T>`** is the author's chosen buffer type for ALL
    packets (even clientbound and unit ones). `ByteBufCodecs.*` are `StreamCodec<ByteBuf,X>` and
    are accepted because `RegistryFriendlyByteBuf extends FriendlyByteBuf extends ByteBuf`. Match
    this convention; do not use plain `FriendlyByteBuf`.
11. **`PacketDistributor` is now all static methods** -- no more `.PLAYER.with(...)` /
    `.TRACKING_ENTITY_AND_SELF.with(...)` builder/supplier pattern.
12. **TYPE id strings are the wire identity** -- choose a stable unique snake_case path per packet
    under the mod namespace; never reuse the same string for two packets.
13. **Keep the manager send helpers** (retyped to `CustomPacketPayload`) so the ~16 call sites in
    `FrozenEffectHandler`, `ClientSetup`, `WeaponVisualEffectHelper`, `SickleThrowSkill` need NO
    changes -- every packet now implements `CustomPacketPayload`, so they fit the param type.
14. **`Type` is `CustomPacketPayload.Type`** -- usable unqualified inside a class that
    `implements CustomPacketPayload` (inherited member type). `new Type<>(rl)` and the
    `type()` return type `Type<Rec>` both resolve without an extra import.
