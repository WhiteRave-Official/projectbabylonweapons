export const meta = {
  name: 'pbw-playbook',
  description: 'Extract a verified Forge 1.20.1 -> NeoForge 1.21.1 porting playbook for Project Babylon Weapons',
  phases: [{ title: 'Playbook', detail: 'parallel domain agents: read Material git diffs + introspect real jars -> verified remap tables' }],
}

const WEAPONS = 'C:/Users/franc/IdeaProjects/PrjBabylonWeaponsNeo'
const MATERIAL = 'C:/Users/franc/IdeaProjects/PrjBabilonMaterialPort'
const SCRATCH = 'C:/Users/franc/AppData/Local/Temp/claude/C--Users-franc-IdeaProjects-PrjBabylonWeaponsNeo/0b4b9299-fadc-414b-a59c-93d1065561bd/scratchpad'
const PLAYBOOK = SCRATCH + '/playbook'
const MERGED = WEAPONS + '/build/moddev/artifacts/neoforge-21.1.233-merged.jar'
const NFSRC = WEAPONS + '/build/moddev/artifacts/neoforge-21.1.233-sources.jar'

const SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['domain', 'playbookFile', 'importRemaps', 'topRecipes', 'risks'],
  properties: {
    domain: { type: 'string' },
    playbookFile: { type: 'string', description: 'absolute path to the .md you wrote' },
    importRemaps: {
      type: 'array',
      description: 'EVERY import-line transformation you are confident is a pure rename (safe for blind find/replace). Omit anything that also needs code changes.',
      items: {
        type: 'object', additionalProperties: false,
        required: ['from', 'to', 'pure'],
        properties: {
          from: { type: 'string', description: 'fully-qualified import WITHOUT "import " prefix or trailing ;' },
          to: { type: 'string' },
          pure: { type: 'boolean', description: 'true if ONLY the import string changes and all usages stay identical' },
        },
      },
    },
    topRecipes: { type: 'array', items: { type: 'string' }, description: 'short one-line descriptions of the key non-trivial transformations in this domain' },
    risks: { type: 'array', items: { type: 'string' } },
  },
}

const HEADER = `You are extracting a VERIFIED porting playbook for migrating a Minecraft mod from Forge 1.20.1 to NeoForge 1.21.1.

The mod being ported: Project Babylon Weapons at ${WEAPONS} (Forge 1.20.1 source, still un-ported).
GOLD REFERENCE: the sibling mod "Project Babylon Materials" was already ported by the same author. Its git history at ${MATERIAL} has branch 'main' (Forge 1.20.1) and 'neo_1.21.1' (NeoForge 1.21.1). Diff them to see the author's EXACT before/after for each subsystem:
  cd "${MATERIAL}" && git diff main neo_1.21.1 -- <path glob>
  git log main..neo_1.21.1 --oneline   (lists the per-subsystem commits)
Use the author's conventions wherever they exist — match them, do not invent your own style.

VERIFY every target API against the REAL jars (do not guess signatures). Techniques (Bash tool, javap + unzip are available):
  - Vanilla 1.21.1 + NeoForge (Mojmang names): javap -p -cp "${MERGED}" <fqcn>   e.g. net.minecraft.world.item.SwordItem
  - NeoForge SOURCES (full source, best for reading): unzip -p "${NFSRC}" path/to/File.java   (list: unzip -l "${NFSRC}" | grep ...)
  - A dependency jar's package layout: unzip -l "<jar>" | grep -E 'pattern'
  - A dependency class signature: javap -p -cp "<jar>" <fqcn>
Resolve dependency jar paths with find, e.g.:
  GECKO=$(find ~/.gradle/caches/modules-2 -name 'geckolib-neoforge-1.21.1-*.jar' | grep -v sources | head -1)
  EF=$(find ~/.gradle/caches/modules-2 -path '*epic*fight*' -name '*.jar' | grep -v sources | head -1)
  IRONS=$(find ~/.gradle/caches/modules-2 -iname 'irons*spell*.jar' | grep -v sources | head -1)

DELIVERABLE: write a thorough, example-rich markdown playbook to the given file path with the Write tool. It must contain:
  1) An import/package remap table (from -> to), marking each as PURE (string-only swap) or SEMANTIC (usages also change).
  2) Concrete before/after code recipes for every non-trivial transformation in this domain, copied/adapted from the Material diff and verified against the jars.
  3) Gotchas / things that differ from a naive port.
Then return the structured summary. The 'importRemaps' field must list EVERY pure-or-semantic import remap you found (set pure correctly) so a downstream mechanical pass can apply the pure ones. Be exhaustive and precise; downstream agents will port 291 files using ONLY your playbook + the jars.`

const DOMAINS = [
  {
    id: '01-mod-entry-registration',
    title: 'Mod entry point & registration',
    focus: `Forge mod bootstrap -> NeoForge. Cover: @Mod main class (find it: grep -rl '@Mod(' ${WEAPONS}/src/main/java), the mod constructor (IEventBus modBus injection), FMLJavaModLoadingContext removal, mod-bus vs game-bus (NeoForge.EVENT_BUS), @Mod.EventBusSubscriber -> @EventBusSubscriber (net.neoforged.fml.common.EventBusSubscriber) with bus selection, Dist/OnlyIn (net.neoforged.api.distmarker), DeferredRegister + DeferredRegister.Items/Blocks, RegistryObject -> DeferredHolder, register(...) lambda style, RegisterEvent, creative mode tabs (CreativeModeTab via DeferredRegister + BuildCreativeModeTabContentsEvent), FMLClientSetupEvent/FMLCommonSetupEvent.
Material ref commits: 'Update registration classes and mod entry points', 'Migrate mod manifest'. Diff: git diff main neo_1.21.1 -- '*/init/*' and the main mod class.
Weapons dirs: src/main/java/com/rave/projectbabylonweapons/init, gameasset, and the @Mod class.`,
  },
  {
    id: '02-items-data-components',
    title: 'Items & data components (1.20.5+ overhaul)',
    focus: `The biggest vanilla change. Cover: Item.Properties (now requires setId/registration assoc? verify), Tier vs ToolMaterial, constructors for SwordItem/PickaxeItem/AxeItem/ShovelItem/HoeItem/DiggerItem/TieredItem/Item (verify each with javap on ${MERGED}), attribute modifiers moved from getAttributeModifiers/Multimap to the ItemAttributeModifiers DATA COMPONENT set via Item.Properties (e.g. SwordItem.createAttributes / Tool component), appendHoverText signature change (ItemStack, Item.TooltipContext, List<Component>, TooltipFlag) + how to get level from TooltipContext, ItemStack NBT -> DataComponents (getOrCreateTag/getTag/getTagElement removed -> get/set/getOrDefault with DataComponentType; CUSTOM_DATA component / CustomData), Rarity (now Rarity enum + Component rarity via component? verify), use()/useOn signatures, InteractionResultHolder.
Material ref commits: 'Adapt crafting blocks, block entities', 'Port loot modifiers and item rarity system', 'Port gem application, slot, and upgrade helpers'. Diff those.
Weapons dirs: item/** (battleaxe, battlehammer, battlescythe, claws, dagger, greatsword, longsword, rapier, shortsword, sickle, spear, staff, tachi, tool, wand, special, material), tooltip/, util/. Read several representative item classes to capture the real patterns.`,
  },
  {
    id: '03-networking',
    title: 'Networking (payload system)',
    focus: `Forge SimpleChannel -> NeoForge payload API. Cover: SimpleChannel/NetworkRegistry/channel.messageBuilder -> PayloadRegistrar via RegisterPayloadHandlersEvent (registrar.playToServer/playToClient), message classes -> records implementing CustomPacketPayload with a CustomPacketPayload.Type<T> and a StreamCodec<RegistryFriendlyByteBuf/ByteBuf, T>, NetworkEvent.Context / PlayMessages -> IPayloadContext (ctx.player(), ctx.enqueueWork(), ctx.reply()), encode/decode(FriendlyByteBuf) -> StreamCodec, sending (PacketDistributor.sendToServer/sendToPlayer). Verify all against ${NFSRC}: unzip -p sources for CustomPacketPayload, StreamCodec, IPayloadContext, PayloadRegistrar, RegisterPayloadHandlersEvent, PacketDistributor.
Material ref commit: 'Port networking to NeoForge 1.21.1 packet channel API'. Diff: git diff main neo_1.21.1 -- '*/network/*' (or wherever).
Weapons dir: network/. Read every file there.`,
  },
  {
    id: '04-events-handlers',
    title: 'Events & event handlers',
    focus: `Cover the event package moves and the TickEvent split. net.minecraftforge.event.* -> net.neoforged.neoforge.event.* (verify each subpackage: entity.living, entity.player, level, tick, etc). CRITICAL: Forge TickEvent (PlayerTickEvent/ClientTickEvent/ServerTickEvent/LevelTickEvent with phase==START/END) is REPLACED by separate classes net.neoforged.neoforge.event.tick.{ServerTickEvent,LevelTickEvent}.{Pre,Post} and net.neoforged.neoforge.client.event.ClientTickEvent.{Pre,Post} and net.neoforged.neoforge.event.tick.PlayerTickEvent.{Pre,Post} (verify exact FQCNs via unzip -l ${NFSRC} | grep -i tickevent). LivingEvent/LivingHurtEvent (now LivingDamageEvent / LivingIncomingDamageEvent? verify), MobEffectEvent, PlayerEvent, AttackEntityEvent, RegisterCommandsEvent, AddReloadListenerEvent. Also @SubscribeEvent (net.neoforged.bus.api.SubscribeEvent) + which bus each event is on (mod bus vs NeoForge.EVENT_BUS game bus) — list which common events moved buses.
Material ref commit: 'Update combat helpers and event handlers for NeoForge event system'. Diff its handler files.
Weapons dirs: handler/, effect/, passive/**, world/entity/effect/. Read representative handlers.`,
  },
  {
    id: '05-client-render',
    title: 'Client & rendering (non-GeckoLib)',
    focus: `Cover: client setup events, RegisterRenderers / EntityRenderersEvent, BlockEntityWithoutLevelRenderer + IClientItemExtensions (Forge IItemRenderProperties -> NeoForge IClientItemExtensions via registerClientExtensions / RegisterClientExtensionsEvent), model layer registration (EntityRenderersEvent.RegisterLayerDefinitions), RenderLevelStageEvent, KeyMapping registration (RegisterKeyMappingsEvent), particle factory registration (RegisterParticleProvidersEvent), sound, camera, GuiGraphics (mostly same as 1.20.1), ItemProperties/ClampedItemPropertyFunction, custom GUI Screen render(GuiGraphics,...) signatures. Verify via ${NFSRC}.
Material ref commits: 'Update GUI screens for 1.21.1 screen rendering API', 'Port client-side renderers, overlays, and Photon effect helper'.
Weapons dirs: client/** (camera, input, model, particle, renderer, sound), block/renderer, block/model, item/renderer (non-geckolib parts). NOTE: GeckoLib specifics are covered by a separate agent — only cover the non-GeckoLib client/render plumbing here, but DO note where the two interact.`,
  },
  {
    id: '06-geckolib',
    title: 'GeckoLib 4.5.x -> 4.8.2 (NeoForge)',
    focus: `Resolve the jar: GECKO=$(find ~/.gradle/caches/modules-2 -name 'geckolib-neoforge-1.21.1-*.jar' | grep -v sources | head -1). The mod was written against geckolib 4.5.x with a 'software.bernie.geckolib.core.*' package tree that NO LONGER EXISTS in 4.8.x. Produce the COMPLETE package remap by listing the new layout: unzip -l "$GECKO" | grep 'software/bernie/geckolib' | sed 's#/[^/]*\\.class##' | sort -u. Map every old path the mod uses to the new one. Known: software.bernie.geckolib.core.animation.* -> software.bernie.geckolib.animation.* ; verify RawAnimation, AnimationController, AnimatableManager(.ControllerRegistrar), AnimationState, PlayState, GeoAnimatable, GeoItem, GeoBlockEntity, GeoEntity, AnimatableInstanceCache (GeckoLibUtil.createInstanceCache), DefaultedItemGeoModel / GeoModel, GeoItemRenderer / GeoBlockRenderer / GeoEntityRenderer / GeoArmorRenderer, RenderUtils -> (RenderUtil?), the registerControllers(ControllerRegistrar) signature, getAnimatableInstanceCache, and the Animation/Keyframe APIs. Use javap on the jar for exact signatures. Cross-check actual usage in the mod (grep software.bernie.geckolib in ${WEAPONS}/src/main/java).
Weapons dirs: item/model, item/renderer, block/model, block/renderer, client/model, and anywhere geckolib is imported.`,
  },
  {
    id: '07-epicfight-caps-anim',
    title: 'Epic Fight 1.20 -> 21.17.3.1: capabilities, presets, animations',
    focus: `Resolve: EF=$(find ~/.gradle/caches/modules-2 -path '*epic*fight*' -name '*.jar' | grep -v sources | head -1). This is the core integration. Focus on the CAPABILITY/ANIMATION side: yesman.epicfight.world.capabilities.item.* (CapabilityItem, CapabilityItem.Builder, WeaponCapabilityPresets, CapabilityItem.Styles), the deprecated->new livingMotionModifier: old livingMotionModifier(Style, LivingMotion, StaticAnimation) is replaced by livingMotionModifier(Style, LivingMotion, AnimationAccessor<? extends StaticAnimation>) — find AnimationAccessor (verify FQCN + how to wrap a StaticAnimation into an AnimationAccessor; check how EF's own code / AnimationManager registers and exposes animations now), StaticAnimation, AttackAnimation, the animation registration system (AnimationManager / Animations registry / how custom animations are built and referenced now), Armatures, yesman.epicfight.api.* moves, yesman.epicfight.gameasset.* (Animations, EpicFightSkills?, Armatures, EpicFightSounds, EpicFightParticles), EpicFightAttributes, EpicFightCapabilities. Map old package paths to new. Use unzip -l "$EF" | grep ... and javap for signatures. Cross-check usage: grep -rn 'yesman.epicfight' ${WEAPONS}/src/main/java/com/rave/projectbabylonweapons/world/capabilities ${WEAPONS}/src/main/java/com/rave/projectbabylonweapons/gameasset.
Weapons dirs: world/capabilities/**, gameasset/, passive/** (these define per-material livingMotion/animation presets).`,
  },
  {
    id: '08-epicfight-skills',
    title: 'Epic Fight 1.20 -> 21.17.3.1: skills & skill data',
    focus: `Resolve EF jar as above. Focus on the SKILL side: yesman.epicfight.skill.* (Skill, Skill.Builder, SkillDataManager, SkillContainer, the onInitiate / onExecution / getStateFactor / etc. method signatures that CHANGED — the compiler reports 'method onInitiate in class Skill cannot be applied'; find the new signature via javap -p on Skill and subclasses), SkillCategory, the weapon_innate skills, skill registration (EpicFightSkills / Skills registry, RegisterSkillEvent?), SkillDataKeys / SkillDataManager generics, PlayerEventListener (yesman.epicfight.world.entity.eventlistener.PlayerEventListener — verify new package, the 'eventlistener' package error), EventType. Map old->new packages and document the changed method signatures with before/after. Use javap heavily.
Weapons dirs: skill/**, skill/weapon_innate/**. Read every skill file to capture the override-signature mismatches (there are ~554 'does not override' errors mod-wide; many are EF skill overrides).`,
  },
  {
    id: '09-resources-misc-vanilla',
    title: 'Resources layout & misc vanilla 1.21.1',
    focus: `Cover: (A) ResourceLocation — new ResourceLocation(ns,path) is private; use ResourceLocation.fromNamespaceAndPath(ns,path) / ResourceLocation.parse(str) / .withDefaultNamespace; verify on ${MERGED}. (B) Holder<Attribute> — attributes are now Holder<Attribute> (e.g. Attributes.ATTACK_DAMAGE is Holder<Attribute>); AttributeModifier now takes a ResourceLocation id not a UUID+name (verify AttributeModifier ctor). (C) RegistryFriendlyByteBuf vs FriendlyByteBuf. (D) SoundEvent registration (SoundEvent.createVariableRangeEvent), ParticleType registration, MobEffect ctor + MobEffectInstance, registration of effects via DeferredRegister. (E) Data resource layout differences for 1.21.1: tags/recipes/loot_table(s) folder rename (loot_tables -> loot_table? verify from Material 'Restructure data resources' commit), recipe json 'category', sounds.json, lang. Diff Material commit 'Restructure data resources: drop Forge paths, add NeoForge 1.21.1 layout' AND 'Update config, status effects'. (F) net.minecraftforge.common / ForgeConfigSpec -> net.neoforged.neoforge.common ModConfigSpec; capability attach (AttachCapabilitiesEvent removed -> data attachments / RegisterCapabilitiesEvent) if used. (G) Tags (TagKey, BlockTags/ItemTags), DamageSource (now via registry Holder).
Weapons dirs: config/, effect/, world/entity/effect/, util/, and src/main/resources data tree (list it). Diff Material data-resource commit thoroughly.`,
  },
]

phase('Playbook')
const results = await parallel(DOMAINS.map(d => () =>
  agent(
    `${HEADER}\n\n=== YOUR DOMAIN: ${d.title} ===\n${d.focus}\n\nWrite your playbook to: ${PLAYBOOK}/${d.id}.md\nThen return the structured summary.`,
    { label: d.id, phase: 'Playbook', schema: SCHEMA }
  )
))

const ok = results.filter(Boolean)
log(`Playbook: ${ok.length}/${DOMAINS.length} domains complete`)
return {
  completed: ok.length,
  total: DOMAINS.length,
  domains: ok.map(r => ({ domain: r.domain, file: r.playbookFile, remaps: (r.importRemaps || []).length, recipes: r.topRecipes, risks: r.risks })),
  allImportRemaps: ok.flatMap(r => (r.importRemaps || []).map(m => ({ ...m, domain: r.domain }))),
}
