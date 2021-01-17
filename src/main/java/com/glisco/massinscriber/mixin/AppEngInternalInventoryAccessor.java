package com.glisco.massinscriber.mixin;

import appeng.tile.inventory.AppEngInternalInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AppEngInternalInventory.class)
public interface AppEngInternalInventoryAccessor {

    @Accessor("maxStack")
    int[] getMaxStack();
}
