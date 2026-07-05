# Port handoff — Project Babylon Weapons: Forge 1.20.1 → NeoForge 1.21.1

Branch: `neoforge_1.21.1`. Reference (already ported, same author): `../PrjBabilonMaterialPort`
(branches `main` = Forge, `neo_1.21.1` = NeoForge — diff them per subsystem).

## How to resume in a NEW session
Say: **"continua il port di NeoForge, leggi port-notes/HANDOFF.md"**.
The agent should: read this file + `port-notes/playbook/*.md`, then run the compile-fix loop.
NOTE: a workflow `resumeFromRunId` is **same-session only** — it will NOT resume across sessions.
To regenerate the missing playbook docs run the saved workflow `/pbw-playbook` (or `Workflow({name:"pbw-playbook"})`)
but FIRST edit it to skip the 2 already-done domains (geckolib, networking) to save tokens.

## Current state
- **Foundation DONE & committed** (build config, manifest, deps, geckolib fix). Gradle resolves all deps and compiles.
- **Baseline: 3720 compile errors** (`port-notes/last-compile.log`). Run `./gradlew compileJava --console=plain` to reproduce.
- **No code ported yet** — the 291 `.java` files are still Forge 1.20.1.
- Playbook: 2 of 9 domains extracted (`port-notes/playbook/03-networking.md`, `06-geckolib.md`). Other 7 died to a network outage.

## Foundation changes already made (committed)
- `build.gradle`: rewritten to `net.neoforged.moddev 2.0.78` plugin (modeled on Material's). Java 21.
- `gradle.properties`: NeoForge style. `neo_version=21.1.233`, parchment 1.21.1/2024.11.17, MC 1.21.1. Dep version props at bottom.
- `settings.gradle`: NeoForged maven, foojay 0.9.0.
- `gradle/wrapper`: bumped 8.8 → 8.12.
- `src/main/resources/META-INF/mods.toml` → **deleted**; created `neoforge.mods.toml` (type=required, deps: neoforge/minecraft/epicfight/project_babylon_materials/irons_spellbooks/geckolib required, acgbattlescytherevived optional). **No mixin block** (mod has zero mixins).
- `pack.mcmeta`: format 15 → 34 (+ supported_formats [34,57]).
- `build.gradle` compiler args: `-Xmaxerrs 100000` (so the full error list prints).

## Key decisions / gotchas
- **GeckoLib**: the bare `maven.modrinth:geckolib:4.9.1` ships INTERMEDIARY-mapped renderer base classes (`class_756`/`class_827`) that fail to subclass Mojmap MC. Fixed by vendoring the Mojmap NeoForge jar into `libs/geckolib-neoforge-1.21.1-4.8.2.jar` (referenced via `compileOnly files(...)`). DO NOT revert to the Modrinth coordinate.
- **Compile-time third-party deps are ONLY 4**: epicfight, geckolib, irons_spells, project_babylon_materials. Nothing else is imported in source (no JEI/photon/ldlib/curios/moonlight/weapons-of-miracles).
- **project_babylon_materials**: consumed as a local `files(...)` jar from `../PrjBabilonMaterialPort/build/libs/project_babylon_materials-1.1.15.jar` (compileOnly + localRuntime). Material must be built first (it is).
- `battlescythe` = the mod's OWN internal package, NOT the external ACG addon.
- The mod has **zero mixins**.

## Reference jars for API verification (use javap/unzip, NOT guessing)
- Vanilla 1.21.1 + NeoForge (Mojmap): `build/moddev/artifacts/neoforge-21.1.233-merged.jar` (+ `-sources.jar` for source)
- Epic Fight: `~/.gradle/caches/modules-2/.../epic-fight-21.17.3.1-mc1.21.1-neoforge.jar`  (find: `find ~/.gradle/caches/modules-2 -path '*epic*fight*' -name '*.jar' | grep -v sources`)
- GeckoLib: `libs/geckolib-neoforge-1.21.1-4.8.2.jar`
- Irons Spells: `find ~/.gradle/caches/modules-2 -iname 'irons*spell*.jar' | grep -v sources`
- Techniques: `javap -p -cp <jar> <fqcn>` ; `unzip -p <sourcesjar> path/File.java` ; `unzip -l <jar> | grep ...`

## Error buckets (from baseline; many "cannot find symbol" are cascades from failed imports)
| count | nature |
|---|---|
| 1574 | `cannot find symbol` (mostly cascades — shrink after imports/registration fixed) |
| 554 | `@Override` broke (supertype signature drift: vanilla/EF/geckolib) |
| 188 | `SwordItem(Tiers,int,float,Properties)` no such ctor → **data components** |
| ~290 | geckolib `core.*` packages gone |
| ~500 | Forge→Neo package imports |
| 158 | `Level → Item.TooltipContext` (appendHoverText) |
| ~150 | Epic Fight API drift (eventlistener, Skill, sounds/particles/attributes) |
| ~90 | `Attribute→Holder<Attribute>`, `ResourceLocation` ctor private, `FriendlyByteBuf→CompoundTag` |

## Heaviest files (port registration FIRST — clears cascades)
PBModItems(252), FrozenDebuffIceBlockTileEntity(88), PBModEntities(70), PBAnimations(70),
then weapon item families (~60 each), skills (skill/weapon_innate, 46-68 each),
PBWeaponCapabilityPresets(54), EpicFightIntegration(40), handlers (FrozenEffectHandler 58, FrozenAttackHandler 40).

## VERIFIED recipes (already confirmed against jars/source)

### Item constructor (all SwordItem-family weapons, ~90 files)
Before: `super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY))`
After:  `super(Tiers.WOOD, props.durability(DURABILITY).attributes(SwordItem.createAttributes(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD)))`
(verified: `SwordItem(Tier, Item.Properties)` + `SwordItem.createAttributes(Tier,int,float)`)

### appendHoverText (every item with a tooltip)
Before: `appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag)` + `super.appendHoverText(stack, level, tooltip, flag)`
After:  `appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)` + `super.appendHoverText(stack, context, tooltip, flag)`
Drop `import net.minecraft.world.level.Level` if now unused; add `net.minecraft.world.item.Item.TooltipContext` (or qualify as `Item.TooltipContext`).

### Networking (see port-notes/playbook/03-networking.md — full recipes)
Forge SimpleChannel msg → `record X(...) implements CustomPacketPayload` with `CustomPacketPayload.Type<X> TYPE`,
`StreamCodec<...> STREAM_CODEC`, `type()` override, `handle(X, IPayloadContext)`. Register in PBNetworkManager via
`RegisterPayloadHandlersEvent` → `registrar.playToClient/playToServer(TYPE, STREAM_CODEC, X::handle)`.
Send via `PacketDistributor.sendToServer(...)` / `PacketDistributor.sendToPlayer(serverPlayer, ...)`.

### GeckoLib (see port-notes/playbook/06-geckolib.md — full)
Pure import remaps (8): `software.bernie.geckolib.core.animation.*` → `...animation.*`;
`core.object.PlayState` → `animation.PlayState` (NOT object.PlayState!);
`core.animatable.instance.AnimatableInstanceCache` → `animatable.instance.AnimatableInstanceCache` (keep instance subpkg);
`core.animatable.GeoAnimatable` → `animatable.GeoAnimatable`; `util.RenderUtils` → `util.RenderUtil`.
SEMANTIC (11 renderer files): `actuallyRender(...)` override — replace trailing `float red,green,blue,alpha` with single `int renderColor`, in BOTH the declaration and the `super.actuallyRender(...)` call. Invisible to the import pass — must be done or override silently breaks.

## Remaining playbook domains to extract (or do directly while porting)
01 mod-entry/registration, 02 items/data-components, 04 events (TickEvent split!), 05 client/render,
07 epicfight caps/animations (livingMotionModifier now takes `AnimationAccessor<? extends StaticAnimation>`),
08 epicfight skills (Skill.onInitiate signature changed — javap it), 09 resources/misc-vanilla.

## Recommended port order (next steps)
1. Apply pure import remaps across all files (Forge→Neo + geckolib core→flat) via script → recompile.
2. Port registration classes (PBModItems, PBModEntities, PBModBlocks, PBAnimations, init/*, EpicFightIntegration).
3. Port weapon item families (template recipe above) — one variant then replicate.
4. Port networking, then events/handlers, then EF capabilities/presets, then EF skills, then client/render, block entities, projectiles, effects, config.
5. Compile-fix loop until BUILD SUCCESSFUL. Then `./gradlew build`.
6. Port data resources (recipes/loot/tags/lang/sounds) per Material's "Restructure data resources" commit (loot_tables→loot_table folder rename etc.).

---
# WAVE 2 — porting the `main` 1.20.1 delta (in progress, 2026-07-01)

The Forge `main` branch was updated with fixes + features. We imported that delta and are porting it.

## What was done
- `git diff 4dd541c main` = the new changes. Imported main's Forge code for all changed files into the working tree (commit "WIP: import main 1.20.1 delta ... pre-port"), then 5 cluster agents ported them. **3 agents were cut off by a session limit** — port is INCOMPLETE and does NOT compile yet. All partial work committed ("WIP: partial NeoForge port of main delta").
- New content: shield family (item/shield/**, Bastion+Small), SternSlamSkill, PBLongswordStyles, new entities (DiamondShard, DragonFuryCharge), bastion/smallshield passives.
- Effects were DELETED from weapons (moved to the Materials parent mod) — weapons now uses `com.rave.projectbabylonmaterials.init.PBMEffects`.
- Added `cdmoveset-*.jar` to the compile fileTree in build.gradle.

## BLOCKERS
1. **Materials effects missing (NEEDS USER / materials update).** Weapons `main` uses ~16 effects from `com.rave.projectbabylonmaterials.init.PBMEffects`: BLEED_DEBUFF, FROZEN, FEAR_DEBUFF, MARKED, CONCUSSED, CHAINED, BRIMSTONE_FIRE, BRIMSTONE_FLAMES, CRIT_RESISTANCE, PHYSICAL_RESISTANCE, MAGICAL_RESISTANCE, ASH_MEMORY, HOLY_SIGIL, WEAPON_CHIP, EXHAUSTED, PROVOKE_DEBUFF. The ported Materials mod (../PrjBabilonMaterialPort, jar project_babylon_materials-1.1.15.jar) `PBMEffects` defines ONLY `UNSTABLE`. These effects exist in NEITHER materials branch. → The Materials 1.21.1 mod must be updated with these effects and rebuilt (`gradlew build`) so weapons can compile/run. Some effect classes (BleedDebuff, FrozenDebuff, FearDebuff, Marked, Concussed, BrimstoneFlames, BrokenArmor, ImpactBreak, MagicBrokenArmor) exist as ALREADY-PORTED NeoForge code in weapons git history (commit d6633b5, before the delta deleted them) and can be moved into materials; the rest (CRIT/PHYSICAL/MAGICAL_RESISTANCE, ASH_MEMORY, HOLY_SIGIL, WEAPON_CHIP, EXHAUSTED, PROVOKE_DEBUFF) are new and need their source.
   NOTE: cdmoveset jar does NOT contain these effects (checked).
2. **net.corruptdog.cdm — RESOLVED.** cdmoveset-2.0-neoforge1.21.1.jar (curse.maven:epic-fight-resurrection-fork-1514242:7927181) in run/mods provides net.corruptdog.cdm (CorruptAnimations, CDSkills). Added to build.gradle compile fileTree.

## USER NOTE
- `main` (1.20.1) got ANOTHER latest commit after the delta was imported — RE-DIFF `4dd541c..main` (or the new HEAD) to catch anything additional to port.
- Weapons must run alongside the user's Materials port (../PrjBabilonMaterialPort build) — its jar is on compileOnly + localRuntime.

## NEXT STEPS (fresh session)
1. Verify ProjectBabylonWeapons.java + PBWeaponCapabilityPresets.java still have the runtime fixes (mod-bus ctor, PBSkills.REGISTRY.register, PBNetworkManager listener, PBWeaponCapabilityPresets.register() via EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET, addPackFinders "resourcepacks/projectbabylonpack", NO gameBus.register(this)) — the init agent was cut off mid-work.
2. Resolve blocker 1 (materials effects).
3. `./gradlew compileJava` and finish porting the remaining delta errors (skill/passive/handler/client/network were the cut-off clusters).
4. Then: runtime test (was working before the delta), and the still-pending data-resource migration (recipes/loot/lang layout, GeckoLib idle animations, EF animation format).

---
# WAVE 2 COMPLETE (2026-07-01): main delta ported, builds, resources synced

- Weapons + Materials both compile & build. main delta (shields, stern_slam, entities, effect-move) fully ported.
- Materials mod updated: PBMEffects now has all 21 effects + PBMPhotonEffectHelper Bastion auras (committed in ../PrjBabilonMaterialPort neo_1.21.1). Fresh jar copied to weapons run/mods.
- cdmoveset (net.corruptdog.cdm) added to compile classpath.
- FIXED a big gap: the resource delta import missed 394 files (git pathspec). Restored all resources present on main but missing from the branch (animations, shield textures/models). Java verified complete (HEAD == main).
- EF animation clips migrated to 21.x format: clip JSONs must be `{"animation":[...]}` only (old bundled vertices/armature/constructor removed). 115 clips converted. Non-clip EF files (trail_effects, layer/masks/priority, multilayer) left as-is.
- Materials Photon FX runtime is a no-op stub on neo_1.21.1 (ALL effects, not just bastion) → particle visuals won't render until that shared stub is implemented (non-fatal).

## Still possibly pending (runtime polish)
- EF datapack animations (data/epicfight/...) with "No constructor information" (~non-fatal earlier) may need a `constructor` field for 21.x.
- GeckoLib idle-animation lookups, recipe/loot/lang 1.21.1 layout — verify at runtime.
- Runtime: retest weapon use after the missing-clip fix.
