package com.rave.projectbabylonweapons.passive.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class WeaponPassivePatchManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String PATCH_FOLDER = "weapon_passive_patches";

    public static final WeaponPassivePatchManager INSTANCE = new WeaponPassivePatchManager();

    private volatile List<WeaponPassivePatch> patches = List.of();

    private WeaponPassivePatchManager() {
        super(GSON, PATCH_FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<WeaponPassivePatch> loadedPatches = new ArrayList<>();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> parsePatch(entry.getKey(), entry.getValue(), loadedPatches));

        this.patches = List.copyOf(loadedPatches);
        ProjectBabylonWeapons.LOGGER.info("Loaded {} weapon passive patch entries", this.patches.size());
    }

    @Nullable
    public <T> T resolveProfile(ResourceLocation passiveId, ItemStack stack, Function<JsonObject, T> parser) {
        WeaponPassivePatch patch = findBestPatch(passiveId, stack);
        if (patch == null) {
            return null;
        }

        try {
            return parser.apply(patch.profile().deepCopy());
        } catch (RuntimeException exception) {
            ProjectBabylonWeapons.LOGGER.error("Failed to parse passive patch {} for {}", patch.id(), passiveId, exception);
            return null;
        }
    }

    @Nullable
    public WeaponPassiveTooltipData resolveTooltipData(ResourceLocation passiveId, ItemStack stack) {
        WeaponPassivePatch patch = findBestPatch(passiveId, stack);
        if (patch == null || patch.visual() == null) {
            return null;
        }

        try {
            return parseTooltipData(patch.visual().deepCopy(), patch.id());
        } catch (RuntimeException exception) {
            ProjectBabylonWeapons.LOGGER.error("Failed to parse passive visual {} for {}", patch.id(), passiveId, exception);
            return null;
        }
    }

    @Nullable
    private WeaponPassivePatch findBestPatch(ResourceLocation passiveId, ItemStack stack) {
        WeaponPassivePatch tagMatch = null;
        WeaponPassivePatch itemMatch = null;

        for (WeaponPassivePatch patch : this.patches) {
            if (!patch.passiveId().equals(passiveId) || !patch.matches(stack)) {
                continue;
            }

            if (patch.matchesItem(stack)) {
                itemMatch = patch;
            } else if (patch.isTagPatch()) {
                tagMatch = patch;
            }
        }

        return itemMatch != null ? itemMatch : tagMatch;
    }

    private void parsePatch(ResourceLocation patchId, JsonElement element, List<WeaponPassivePatch> loadedPatches) {
        if (!element.isJsonObject()) {
            ProjectBabylonWeapons.LOGGER.warn("Skipping passive patch {} because it is not a JSON object", patchId);
            return;
        }

        JsonObject json = element.getAsJsonObject();
        ResourceLocation passiveId = parseRequiredId(json, "passive_id", patchId);
        JsonObject profile = GsonHelper.getAsJsonObject(json, "profile", null);
        if (passiveId == null || profile == null) {
            ProjectBabylonWeapons.LOGGER.warn("Skipping passive patch {} because passive_id/profile is missing", patchId);
            return;
        }

        List<ResourceLocation> weaponIds = parseWeaponIds(json, patchId);
        ResourceLocation weaponTagId = parseOptionalId(json, "weapon_tag", patchId);
        boolean hasIds = !weaponIds.isEmpty();
        boolean hasTag = weaponTagId != null;
        if ((hasIds && hasTag) || (!hasIds && !hasTag)) {
            ProjectBabylonWeapons.LOGGER.warn("Skipping passive patch {} because exactly one of weapon_id, weapon_ids, or weapon_tag must be set", patchId);
            return;
        }

        JsonObject visual = GsonHelper.getAsJsonObject(json, "visual", null);
        loadedPatches.add(new WeaponPassivePatch(
                patchId,
                passiveId,
                weaponIds,
                weaponTagId == null ? null : WeaponPassivePatch.createItemTag(weaponTagId),
                profile.deepCopy(),
                visual == null ? null : visual.deepCopy()
        ));
    }

    private List<ResourceLocation> parseWeaponIds(JsonObject json, ResourceLocation patchId) {
        boolean hasWeaponId = json.has("weapon_id");
        boolean hasWeaponIds = json.has("weapon_ids");
        if (hasWeaponId && hasWeaponIds) {
            ProjectBabylonWeapons.LOGGER.warn("Passive patch {} cannot define both weapon_id and weapon_ids", patchId);
            return List.of();
        }

        if (hasWeaponId) {
            ResourceLocation weaponId = parseOptionalId(json, "weapon_id", patchId);
            if (weaponId == null) {
                return List.of();
            }

            if (!BuiltInRegistries.ITEM.containsKey(weaponId)) {
                ProjectBabylonWeapons.LOGGER.warn("Skipping passive patch {} because weapon item {} is not registered", patchId, weaponId);
                return List.of();
            }

            return List.of(weaponId);
        }

        if (hasWeaponIds) {
            JsonArray weaponIdsArray = GsonHelper.getAsJsonArray(json, "weapon_ids");
            List<ResourceLocation> weaponIds = new ArrayList<>(weaponIdsArray.size());
            for (JsonElement weaponIdElement : weaponIdsArray) {
                ResourceLocation weaponId = parseIdString(weaponIdElement.getAsString(), "weapon_ids", patchId);
                if (weaponId == null) {
                    return List.of();
                }

                if (!BuiltInRegistries.ITEM.containsKey(weaponId)) {
                    ProjectBabylonWeapons.LOGGER.warn("Skipping passive patch {} because weapon item {} is not registered", patchId, weaponId);
                    return List.of();
                }

                weaponIds.add(weaponId);
            }
            return List.copyOf(weaponIds);
        }

        return List.of();
    }

    private WeaponPassiveTooltipData parseTooltipData(JsonObject visual, ResourceLocation patchId) {
        Component displayName = parseDisplayName(visual, patchId);
        ResourceLocation frameTexture = parseRequiredId(visual, "frame_texture", patchId);
        ResourceLocation iconTexture = parseRequiredId(visual, "icon_texture", patchId);
        List<Component> descriptionLines = parseDescriptionLines(visual, patchId);
        TooltipFrameStyle frameStyle = parseFrameStyle(visual, patchId);

        if (displayName == null || frameTexture == null || iconTexture == null) {
            throw new IllegalArgumentException("Passive visual is missing required display_name/frame/icon fields");
        }

        return new WeaponPassiveTooltipData(displayName, frameTexture, iconTexture, descriptionLines, frameStyle);
    }

    @Nullable
    private Component parseDisplayName(JsonObject visual, ResourceLocation patchId) {
        if (visual.has("display_name_key")) {
            return Component.translatable(GsonHelper.getAsString(visual, "display_name_key"));
        }

        if (visual.has("display_name")) {
            return Component.literal(GsonHelper.getAsString(visual, "display_name"));
        }

        ProjectBabylonWeapons.LOGGER.warn("Passive visual {} is missing display_name_key/display_name", patchId);
        return null;
    }

    private List<Component> parseDescriptionLines(JsonObject visual, ResourceLocation patchId) {
        if (visual.has("description_keys")) {
            JsonArray keys = GsonHelper.getAsJsonArray(visual, "description_keys");
            List<Component> lines = new ArrayList<>(keys.size());
            for (JsonElement keyElement : keys) {
                lines.add(Component.translatable(keyElement.getAsString()).withStyle(ChatFormatting.GRAY));
            }
            return List.copyOf(lines);
        }

        if (visual.has("description_lines")) {
            JsonArray linesArray = GsonHelper.getAsJsonArray(visual, "description_lines");
            List<Component> lines = new ArrayList<>(linesArray.size());
            for (JsonElement lineElement : linesArray) {
                lines.add(Component.literal(lineElement.getAsString()).withStyle(ChatFormatting.GRAY));
            }
            return List.copyOf(lines);
        }

        ProjectBabylonWeapons.LOGGER.warn("Passive visual {} does not define description_keys/description_lines; using empty description", patchId);
        return List.of();
    }

    @Nullable
    private TooltipFrameStyle parseFrameStyle(JsonObject visual, ResourceLocation patchId) {
        if (!visual.has("frame_type")) {
            return null;
        }

        String frameType = GsonHelper.getAsString(visual, "frame_type");
        if (frameType.isBlank()) {
            ProjectBabylonWeapons.LOGGER.warn("Passive visual {} has blank frame_type", patchId);
            return null;
        }

        return TooltipFrameStyle.material(frameType);
    }

    @Nullable
    private static ResourceLocation parseRequiredId(JsonObject json, String key, ResourceLocation patchId) {
        if (!json.has(key)) {
            ProjectBabylonWeapons.LOGGER.warn("Passive patch {} is missing required field {}", patchId, key);
            return null;
        }

        return parseIdString(GsonHelper.getAsString(json, key), key, patchId);
    }

    @Nullable
    private static ResourceLocation parseOptionalId(JsonObject json, String key, ResourceLocation patchId) {
        if (!json.has(key)) {
            return null;
        }

        return parseIdString(GsonHelper.getAsString(json, key), key, patchId);
    }

    @Nullable
    private static ResourceLocation parseIdString(String rawId, String key, ResourceLocation patchId) {
        ResourceLocation parsedId = ResourceLocation.tryParse(rawId);
        if (parsedId == null) {
            ProjectBabylonWeapons.LOGGER.warn("Passive patch {} has invalid resource location in {}: {}", patchId, key, rawId);
        }
        return parsedId;
    }
}