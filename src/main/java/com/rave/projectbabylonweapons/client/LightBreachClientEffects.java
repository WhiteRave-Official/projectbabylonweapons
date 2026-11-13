package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonmaterials.client.photon.LightBreachColumnVisual;
import com.rave.projectbabylonmaterials.client.photon.PBMPhotonEffectHelper;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.effect.LightBreachEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT)
public final class LightBreachClientEffects {
    private static final Map<UUID, List<LightBreachColumnVisual>> VISUALS = new HashMap<>();

    private LightBreachClientEffects() {}

    public static void tick(LightBreachEntity cast) {
        if (cast.getColumns().isEmpty()) return;
        long now = cast.level().getGameTime();
        int preparationAge = (int) (now - cast.getPreparedAt());
        var settings = cast.getSettings();
        int activeAge = cast.getActiveSince() >= 0 ? (int) (now - cast.getActiveSince()) : -1;
        if (activeAge >= settings.totalTicks()) {
            remove(cast);
            return;
        }
        float sealOpacity = Mth.clamp(preparationAge / 6.0F, 0, 1);
        if (activeAge >= settings.fadeIn() + settings.activeTicks()) {
            sealOpacity *= Mth.clamp((float) (settings.totalTicks() - activeAge) / settings.fadeOut(), 0, 1);
        }
        if (cast.getCancelSince() >= 0) {
            sealOpacity *= Mth.clamp(1 - (float) (now - cast.getCancelSince()) / settings.fadeOut(), 0, 1);
        }
        if (activeAge < 0 && cast.getCancelSince() < 0) {
            Entity owner = cast.level().getEntity(cast.getOwnerId());
            if (owner != null) PBMPhotonEffectHelper.spawnLightBreachPreparation(owner, preparationAge);
        }
        var visuals = VISUALS.computeIfAbsent(cast.getUUID(), ignored -> cast.getColumns().stream()
                .map(column -> PBMPhotonEffectHelper.createLightBreachColumn(cast, column.base(),
                        settings.columnRadius(), column.height())).toList());
        for (var visual : visuals) {
            visual.update(sealOpacity, activeAge, settings.fadeIn(), settings.activeTicks(), settings.fadeOut());
        }
    }

    public static void remove(LightBreachEntity cast) {
        var visuals = VISUALS.remove(cast.getUUID());
        if (visuals != null) visuals.forEach(LightBreachColumnVisual::close);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        VISUALS.values().forEach(list -> list.forEach(LightBreachColumnVisual::close));
        VISUALS.clear();
    }
}
