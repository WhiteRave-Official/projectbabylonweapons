package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.block.renderer.FrozenDebuffIceBlock;
import com.rave.projectbabylonweapons.block.entity.FrozenDebuffIceBlockTileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ProjectBabylonWeapons.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ProjectBabylonWeapons.MODID);

    public static final DeferredBlock<Block> FROZEN_DEBUFF_ICE_BLOCK =
            BLOCKS.register("frozen_debuff_ice_block", FrozenDebuffIceBlock::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrozenDebuffIceBlockTileEntity>> FROZEN_DEBUFF_ICE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("frozen_debuff_ice_block",
                    () -> BlockEntityType.Builder.of(FrozenDebuffIceBlockTileEntity::new, FROZEN_DEBUFF_ICE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
