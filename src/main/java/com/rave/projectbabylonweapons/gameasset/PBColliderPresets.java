package com.rave.projectbabylonweapons.gameasset;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;

public class PBColliderPresets {
    private static final BiMap<ResourceLocation, Collider> PRESETS = HashBiMap.create();
    public static final Collider SICKLE =  new MultiOBBCollider(3, 0.42, 0.45, 0.6, (double)0.0F, 0.1, (double)-0.2F);
    public static final Collider SCYTHE =  new MultiOBBCollider(3, 0.5, 1.0, 0.9, (double)0.0F, 0.1, (double)-1.3F);
    public static final Collider APPERCUT_SKILL =  new MultiOBBCollider(3, 0.7, 0.7, 0.7, (double)0.0F, 0.1, (double)0.0F);
    public static final Collider TECTONIC_SKILL =  new MultiOBBCollider(3, 1.0, 1.0, 1.3, (double)0.0F, 0.1, (double)-1.0F);
    public static final Collider BEAST_ROAR_FIRST =  new OBBCollider(3.0F, 3.0F, 3.0F, 0.0F, 0.0F, 0.0F);
    public static final Collider BEAST_ROAR_SECOND =  new OBBCollider(4.0F, 4.0F, 4.0F, 0.0F, 0.0F, 0.0F);
    public static final Collider BEAST_ROAR_THIRD =  new OBBCollider(5.0F, 5.0F, 5.0F, 0.0F, 0.0F, 0.0F);

    public static Collider registerCollider(ResourceLocation rl, Collider collider) {
        if (PRESETS.containsKey(rl)) {
            throw new IllegalStateException("Collider named " + rl + " already registered.");
        } else {
            PRESETS.put(rl, collider);
            return collider;
        }
    }
}
