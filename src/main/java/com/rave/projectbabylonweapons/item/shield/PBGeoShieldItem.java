package com.rave.projectbabylonweapons.item.shield;

import com.rave.projectbabylonweapons.item.renderer.PBGeoShieldItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.world.item.ShieldItem;

import java.util.function.Consumer;

public class PBGeoShieldItem extends ShieldItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation modelResource;
    private final ResourceLocation textureResource;
    private static final ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "animations/static_shield.animation.json");

    public PBGeoShieldItem(Properties properties, String modelPath, String texturePath) {
        super(properties);
        this.modelResource = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", modelPath);
        this.textureResource = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", texturePath);
    }

    public ResourceLocation getGeoModelResource() {
        return this.modelResource;
    }

    public ResourceLocation getGeoTextureResource() {
        return this.textureResource;
    }

    public ResourceLocation getGeoAnimationResource() {
        return ANIMATION_RESOURCE;
    }

    private PlayState predicate(AnimationState<PBGeoShieldItem> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("0"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new PBGeoShieldItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }
}