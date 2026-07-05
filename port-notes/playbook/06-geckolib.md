# Porting Playbook 06 — GeckoLib 4.5.x → 4.8.2 (Forge 1.20.1 → NeoForge 1.21.1)

Domain: GeckoLib animation/model/renderer code in Project Babylon Weapons.
Target jar (VERIFIED): `geckolib-neoforge-1.21.1-4.8.2.jar`
(`~/.gradle/caches/modules-2/files-2.1/software.bernie.geckolib/geckolib-neoforge-1.21.1/4.8.2/.../geckolib-neoforge-1.21.1-4.8.2.jar`)

> NOTE: The "gold reference" Material port (`PrjBabilonMaterialPort`) contains **zero** GeckoLib code, so it offers no
> before/after for this domain. **Every** signature below was verified directly with `javap -p` against the 4.8.2 jar.

## TL;DR — what actually changes

1. **The whole `software.bernie.geckolib.core.*` package tree was deleted in 4.x→4.8.** Classes moved up out of `core`.
   - `core.animation.*` → `animation.*`
   - `core.object.PlayState` → `animation.PlayState`  (note: NOT `animation.object`, it moved into the flat `animation` package)
   - `core.animatable.instance.AnimatableInstanceCache` → `animatable.instance.AnimatableInstanceCache` (drop the `core.` only)
   These are **PURE string-only import swaps** — class names and every usage are identical.
2. **`GeoRenderer#actuallyRender` (and `preRender`, `renderFinal`, `renderRecursively`, `applyRenderLayers`) lost the
   trailing `float red, float green, float blue, float alpha` params and gained a single trailing `int renderColor`.**
   This is the ONE non-trivial code change in this domain. 11 renderer files in this mod override `actuallyRender` with the
   old 4-float signature and MUST be rewritten. (No file overrides `preRender`.)
3. Everything else — `GeoModel`, `GeoItemRenderer`, `GeoEntityRenderer`, `GeoBlockRenderer`, `GeoItem`, `GeoEntity`,
   `GeoBlockEntity`, `GeckoLibUtil.createInstanceCache`, `registerControllers(AnimatableManager.ControllerRegistrar)`,
   `getAnimatableInstanceCache`, `AnimationController` ctor `(this, name, 0, this::predicate)`,
   `AnimationController.State.STOPPED`, `RawAnimation.begin().thenLoop()/.thenPlay()`, `PlayState.CONTINUE/STOP`,
   `event.getController()...`, `event.setAndContinue(...)`, `getRenderType(T, ResourceLocation, MultiBufferSource, float)`,
   `renderByItem(...)` — is **API-stable**: only the import package path may change, the code is identical.

---

## 1. Import / package remap table

Legend: **PURE** = blind find/replace of the import line is safe (class name + all usages identical).
**UNCHANGED** = path is already correct in 4.8.2, do not touch.

| Old import (4.5.x, in mod source)                                         | New import (4.8.2)                                                    | Kind      | # files |
|---------------------------------------------------------------------------|----------------------------------------------------------------------|-----------|---------|
| `software.bernie.geckolib.core.animation.AnimatableManager`               | `software.bernie.geckolib.animation.AnimatableManager`               | **PURE**  | 24 |
| `software.bernie.geckolib.core.animation.AnimationController`             | `software.bernie.geckolib.animation.AnimationController`             | **PURE**  | 23 |
| `software.bernie.geckolib.core.animation.AnimationState`                 | `software.bernie.geckolib.animation.AnimationState`                 | **PURE**  | 13 |
| `software.bernie.geckolib.core.animation.RawAnimation`                   | `software.bernie.geckolib.animation.RawAnimation`                   | **PURE**  | 23 |
| `software.bernie.geckolib.core.object.PlayState`                          | `software.bernie.geckolib.animation.PlayState`                       | **PURE**  | 23 |
| `software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache` | `software.bernie.geckolib.animatable.instance.AnimatableInstanceCache` | **PURE**  | 18 |
| `software.bernie.geckolib.animatable.GeoItem`                             | *(unchanged)*                                                        | UNCHANGED | 12 |
| `software.bernie.geckolib.animatable.GeoEntity`                          | *(unchanged)*                                                        | UNCHANGED | 6 |
| `software.bernie.geckolib.animatable.GeoBlockEntity`                     | *(unchanged)*                                                        | UNCHANGED | 1 |
| `software.bernie.geckolib.model.GeoModel`                                | *(unchanged)*                                                        | UNCHANGED | 24 |
| `software.bernie.geckolib.cache.object.BakedGeoModel`                    | *(unchanged)*                                                        | UNCHANGED | 11 |
| `software.bernie.geckolib.util.GeckoLibUtil`                             | *(unchanged)*                                                        | UNCHANGED | 18 |
| `software.bernie.geckolib.renderer.GeoItemRenderer`                      | *(unchanged)*                                                        | UNCHANGED | 11 |
| `software.bernie.geckolib.renderer.GeoEntityRenderer`                    | *(unchanged)*                                                        | UNCHANGED | 12 |
| `software.bernie.geckolib.renderer.GeoBlockRenderer`                     | *(unchanged)*                                                        | UNCHANGED | 1 |

**Only the 6 `core.*` rows need rewriting; all 6 are PURE.** Apply them as literal substring replacements anywhere they
appear (imports, and any rare fully-qualified reference).

### Other 4.8 relocations NOT used by this mod (for reference / safety net if more imports surface)
| Old (4.5)                                              | New (4.8.2)                                          |
|--------------------------------------------------------|------------------------------------------------------|
| `software.bernie.geckolib.core.animatable.GeoAnimatable` | `software.bernie.geckolib.animatable.GeoAnimatable` |
| `software.bernie.geckolib.util.RenderUtils`            | `software.bernie.geckolib.util.RenderUtil` (class **renamed**, drop the `s`) |
| `software.bernie.geckolib.core.animation.AnimationController.State` | `software.bernie.geckolib.animation.AnimationController.State` (nested enum; reached via the remapped `AnimationController` import — no separate import) |

---

## 2. Concrete before/after recipes

### Recipe A — `GeoModel` subclass (item model, entity model, block model). PURE.
No code change beyond imports (and the unrelated `ResourceLocation` change owned by another playbook).
`getModelResource(T)`, `getTextureResource(T)`, `getAnimationResource(T)` are still abstract with the same single-arg signature.

```java
// 4.8.2 — unchanged except import path of GeoModel is already correct
import software.bernie.geckolib.model.GeoModel;            // UNCHANGED path

public class IceGreatswordItemModel extends GeoModel<IceGreatswordItem> {
    @Override public ResourceLocation getAnimationResource(IceGreatswordItem a) { ... }
    @Override public ResourceLocation getModelResource(IceGreatswordItem a)    { ... }
    @Override public ResourceLocation getTextureResource(IceGreatswordItem a)  { ... }
}
```

### Recipe B — `GeoItem` (e.g. `IceGreatswordItem implements GeoItem`). Imports only (all 6 core swaps + GeoItem unchanged).
The animatable API is byte-for-byte stable. Verified:
- `void registerControllers(AnimatableManager.ControllerRegistrar data)` — unchanged. `data.add(controller)` still returns the registrar.
- `AnimatableInstanceCache getAnimatableInstanceCache()` — unchanged.
- `GeckoLibUtil.createInstanceCache(this)` — unchanged (`createInstanceCache(GeoAnimatable)`).
- `new AnimationController(this, "name", 0, this::predicate)` — ctor `(T, String, int, AnimationStateHandler<T>)` exists.
- `private PlayState predicate(AnimationState event)` — `AnimationStateHandler.handle(AnimationState<A>)` returns `PlayState`. Identical.
- `event.getController()` → `AnimationController`; `.getAnimationState() == AnimationController.State.STOPPED` — enum values `RUNNING/TRANSITIONING/PAUSED/STOPPED` all present.
- `.forceAnimationReset()`, `.setAnimation(RawAnimation)` — present.
- `RawAnimation.begin().thenLoop("...")`, `.thenPlay("...")` — present. `PlayState.CONTINUE/STOP` — present.

```java
// ONLY the 6 import lines change; class body is identical.
import software.bernie.geckolib.util.GeckoLibUtil;                                  // UNCHANGED
import software.bernie.geckolib.animation.PlayState;                                // was core.object.PlayState
import software.bernie.geckolib.animation.RawAnimation;                             // was core.animation.RawAnimation
import software.bernie.geckolib.animation.AnimationState;                           // was core.animation.AnimationState
import software.bernie.geckolib.animation.AnimationController;                      // was core.animation.AnimationController
import software.bernie.geckolib.animation.AnimatableManager;                        // was core.animation.AnimatableManager
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;        // was core.animatable.instance.*
import software.bernie.geckolib.animatable.GeoItem;                                 // UNCHANGED

public class IceGreatswordItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    ...
    private PlayState idlePredicate(AnimationState event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.ice_greatsword.idle"));
        return PlayState.CONTINUE;
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController(this, "procedureController", 0, this::procedurePredicate));
        data.add(new AnimationController(this, "idleController", 0, this::idlePredicate));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
```
> NB: `IceGreatswordItem` etc. *also* contain unrelated Forge→NeoForge changes
> (`net.minecraftforge.client.extensions.common.IClientItemExtensions` → `net.neoforged.neoforge.client.extensions.common.IClientItemExtensions`,
> the `SwordItem`/`Tiers` constructor, `getOrCreateTag` → data components). Those belong to other playbooks — leave them to those passes.

### Recipe C — `GeoBlockEntity` (`FrozenDebuffIceBlockTileEntity`). Imports only.
Same animatable API as Recipe B, plus `event.setAndContinue(RawAnimation)` (verified: `AnimationState.setAndContinue` returns `PlayState`). No code change beyond the 6 import swaps.

### Recipe D — `GeoEntity` projectile/effect entities. Imports only (core.* swaps), same as Recipe B.

### Recipe E — `GeoEntityRenderer` / `GeoBlockRenderer` that do NOT override `actuallyRender`. Imports only.
`GeoEntityRenderer<T>` ctor `(EntityRendererProvider.Context, GeoModel<T>)` unchanged.
`GeoBlockRenderer<T>` ctor `(GeoModel<T>)` unchanged.
`render(...)` override signatures unchanged. Example `IceSpellProjectileRenderer` / `FrozenDebuffIceBlockTileRenderer`: no change.

### Recipe F — `GeoItemRenderer` subclass with translucent `getRenderType`, NO `actuallyRender` override. Imports only.
`getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick)` is still a
default method on the `GeoRenderer` interface (verified). The `@Override` remains valid. `renderByItem(ItemStack,
ItemDisplayContext, PoseStack, MultiBufferSource, int, int)` unchanged. `getTextureLocation(T)` unchanged.
`GeoItemRenderer` still `extends BlockEntityWithoutLevelRenderer`, so the `IClientItemExtensions.getCustomRenderer()`
wiring still works.
Example: `FrozenDebuffIceBlockDisplayItemRenderer` — no change.

### ⭐ Recipe G — `actuallyRender` override: drop the 4 color floats, add `int renderColor`. **SEMANTIC — 11 files.**

VERIFIED 4.8.2 signature (identical on GeoItemRenderer, GeoEntityRenderer, GeoBlockRenderer):
```
void actuallyRender(PoseStack, T, BakedGeoModel, RenderType, MultiBufferSource,
                    VertexConsumer, boolean isReRender, float partialTick,
                    int packedLight, int packedOverlay, int renderColor)
```
The 4.5 tail `..., int packedLight, int packedOverlay, float red, float green, float blue, float alpha)`
becomes `..., int packedLight, int packedOverlay, int renderColor)`. Apply to BOTH the method declaration AND the
`super.actuallyRender(...)` call.

**Item renderer — BEFORE (4.5):**
```java
@Override
public void actuallyRender(PoseStack matrixStackIn, DiamondBattleWandItem animatable, BakedGeoModel model, RenderType type,
        MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, boolean isRenderer, float partialTicks,
        int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
    this.currentBuffer = renderTypeBuffer;
    this.renderType = type;
    this.animatable = animatable;
    super.actuallyRender(matrixStackIn, animatable, model, type, renderTypeBuffer, vertexBuilder, isRenderer,
            partialTicks, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    if (this.renderArms) { this.renderArms = false; }
}
```
**AFTER (4.8.2):**
```java
@Override
public void actuallyRender(PoseStack matrixStackIn, DiamondBattleWandItem animatable, BakedGeoModel model, RenderType type,
        MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, boolean isRenderer, float partialTicks,
        int packedLightIn, int packedOverlayIn, int renderColor) {
    this.currentBuffer = renderTypeBuffer;
    this.renderType = type;
    this.animatable = animatable;
    super.actuallyRender(matrixStackIn, animatable, model, type, renderTypeBuffer, vertexBuilder, isRenderer,
            partialTicks, packedLightIn, packedOverlayIn, renderColor);
    if (this.renderArms) { this.renderArms = false; }
}
```

**Entity renderer — BEFORE (4.5)** (`GlacierIceSpikeRenderer`):
```java
@Override
public void actuallyRender(PoseStack poseStack, GlacierIceSpikeEntity animatable, BakedGeoModel model,
        @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
        boolean isReRender, float partialTick, int packedLight, int packedOverlay,
        float red, float green, float blue, float alpha) {
    float scale = animatable.getSpikeScale();
    poseStack.scale(scale, scale, scale);
    super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
            packedLight, packedOverlay, red, green, blue, alpha);
}
```
**AFTER (4.8.2):**
```java
@Override
public void actuallyRender(PoseStack poseStack, GlacierIceSpikeEntity animatable, BakedGeoModel model,
        @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
        boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
    float scale = animatable.getSpikeScale();
    poseStack.scale(scale, scale, scale);
    super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
            packedLight, packedOverlay, renderColor);
}
```

Mechanical rule for this mod (works for all 11 files because each declares exactly one
`..., float red, float green, float blue, float alpha)` param list and one matching call `..., red, green, blue, alpha)`):
- Declaration: replace `float red, float green, float blue, float alpha)` → `int renderColor)`.
- Super call:   replace `red, green, blue, alpha)` → `renderColor)`.

The 11 affected files (all override `actuallyRender`):
```
item/renderer/DiamondBattleWandItemRenderer.java
item/renderer/DragonsteelBattleWandItemRenderer.java
item/renderer/GoldenBattleWandItemRenderer.java
item/renderer/IceBattleHammerItemRenderer.java
item/renderer/IceBattleScytheItemRenderer.java
item/renderer/IceBattleWandItemRenderer.java
item/renderer/IceGreatswordItemRenderer.java
item/renderer/IronBattleWandItemRenderer.java
item/renderer/NetheriteBattleScytheItemRenderer.java
item/renderer/NetheriteBattleWandItemRenderer.java
client/renderer/GlacierIceSpikeRenderer.java
```
(Confirmed: `grep -rl "float red, float green, float blue, float alpha"` = 11 files; `void preRender` overrides = 0.)

### Recipe H — `GeoAnimations` init class. Imports only.
Uses GeoItem purely as an `instanceof` marker. Geckolib import (`animatable.GeoItem`) is UNCHANGED. (The Forge
`@Mod.EventBusSubscriber` / `TickEvent` / `getOrCreateTag` parts belong to the Forge-events and data-component playbooks.)

---

## 3. Gotchas / differences from a naive port

1. **`PlayState` did NOT keep an `object` subpackage.** It is `software.bernie.geckolib.animation.PlayState`, not
   `...animation.object.PlayState`. A naive "replace `core.` with empty" would yield `software.bernie.geckolib.object.PlayState`
   (wrong). Use the exact target `software.bernie.geckolib.animation.PlayState`.
2. **`AnimatableInstanceCache` only loses the `core.` segment**, staying under `animatable.instance.*`. Don't over-flatten it
   to `animatable.AnimatableInstanceCache`.
3. **The color floats → `int renderColor` change is invisible to the import remap pass** — the imports in those 11 files
   (`BakedGeoModel`, `RenderType`, `MultiBufferSource`, `VertexConsumer`, `PoseStack`) are all UNCHANGED. If you only run
   the import swap and don't apply Recipe G, the code compiles-looking but fails `@Override` resolution
   ("method does not override"). Always pair Recipe G with the import pass for renderer files.
4. **Do NOT touch `getRenderType(T, ResourceLocation, MultiBufferSource, float)`** in item renderers. It is a still-present
   default on `GeoRenderer`; the override is valid. (It is no longer declared *directly* on `GeoItemRenderer`/`GeoBlockRenderer`,
   but inheritance via the interface keeps the `@Override` legal.)
5. **`RenderUtils` → `RenderUtil`** (class rename, dropped the trailing `s`) — not used by this mod, but if any straggler
   import appears it is a rename, not just a repackage.
6. **`AnimationController.State`** is a nested enum reached through the (remapped) `AnimationController` import. There is no
   separate import line to change; once `AnimationController`'s import is fixed, `AnimationController.State.STOPPED` resolves.
7. `registerControllers` parameter type `AnimatableManager.ControllerRegistrar` is a `record` in 4.8 (was a class in 4.5),
   but the public surface used (`.add(controller)`) is identical, so no code change.
8. Raw (un-parameterized) `new AnimationController(this, ...)` still compiles (it does in the mod's item classes); the block
   entity uses the parameterized `new AnimationController<FrozenDebuffIceBlockTileEntity>(...)`. Both are fine in 4.8 — leave
   as-is.
9. This mod uses **plain `GeoModel`** everywhere (no `DefaultedItemGeoModel` / `GeoArmorRenderer` / `RenderUtil(s)` usage),
   so those subsystems need no attention here.
