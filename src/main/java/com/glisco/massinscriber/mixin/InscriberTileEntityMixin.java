package com.glisco.massinscriber.mixin;

import appeng.api.config.Upgrades;
import appeng.api.features.InscriberProcessType;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.tile.grid.AENetworkPowerTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.InscriberRecipes;
import appeng.tile.misc.InscriberTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(InscriberTileEntity.class)
public abstract class InscriberTileEntityMixin extends AENetworkPowerTileEntity {

    @Shadow
    private InscriberRecipe cachedTask;

    @Shadow
    @Final
    private AppEngInternalInventory topItemHandler;

    @Shadow
    @Final
    private AppEngInternalInventory bottomItemHandler;

    @Shadow
    @Final
    private AppEngInternalInventory sideItemHandler;

    @Shadow
    protected abstract void setProcessingTime(int processingTime);

    @Shadow
    @Nullable
    public abstract InscriberRecipe getTask();

    @Shadow
    protected abstract boolean hasWork();

    public InscriberTileEntityMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    // ----
    //
    //Warning: Gross hacks ahead
    //
    // ----

    /*
    Since the entire Inscriber behaviour is hardcoded to only ever work with one item per slot, I had
    to replicate a few lines of codes from AE2 in the following mixins to avoid 50 different injections
    into every single spot where the original code doesn't account for multiple items
    */

    //Pretty simple, just set the slot item limits to accept more than one
    @Inject(method = "tickingRequest", at = @At("HEAD"), remap = false)
    public void checkUpgradesTick(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {

        if (((InscriberTileEntity) (Object) this).getInstalledUpgrades(Upgrades.CAPACITY) > 0 && topItemHandler.getSlotLimit(0) != 64) {
            Arrays.fill(((AppEngInternalInventoryAccessor) topItemHandler).getMaxStack(), 64);
            Arrays.fill(((AppEngInternalInventoryAccessor) bottomItemHandler).getMaxStack(), 64);
            Arrays.fill(((AppEngInternalInventoryAccessor) sideItemHandler).getMaxStack(), 64);
        } else if (topItemHandler.getSlotLimit(0) == 64) {
            Arrays.fill(((AppEngInternalInventoryAccessor) topItemHandler).getMaxStack(), 1);
            Arrays.fill(((AppEngInternalInventoryAccessor) bottomItemHandler).getMaxStack(), 1);
            Arrays.fill(((AppEngInternalInventoryAccessor) sideItemHandler).getMaxStack(), 1);
        }

    }

    //This is where the most code had to be replicated, since the standard behaviour just
    //puts ItemStack.EMPTY into all slots
    //
    //This method is injected just before the original code handles the items in input slots
    @Inject(method = "tickingRequest", at = @At(value = "INVOKE", target = "Lappeng/tile/misc/InscriberTileEntity;setProcessingTime(I)V", ordinal = 0), remap = false, cancellable = true)
    public void tick(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {

        if(((InscriberTileEntity) (Object) this).getInstalledUpgrades(Upgrades.CAPACITY) < 1) return;

        setProcessingTime(0);

        InscriberRecipe out = this.getTask();

        if (out.getProcessType() == InscriberProcessType.PRESS) {
            topItemHandler.getStackInSlot(0).setCount(topItemHandler.getStackInSlot(0).getCount() - 1);
            if (topItemHandler.getStackInSlot(0).getCount() == 0) topItemHandler.setStackInSlot(0, ItemStack.EMPTY);

            bottomItemHandler.getStackInSlot(0).setCount(bottomItemHandler.getStackInSlot(0).getCount() - 1);
            if (bottomItemHandler.getStackInSlot(0).getCount() == 0) bottomItemHandler.setStackInSlot(0, ItemStack.EMPTY);
        }

        sideItemHandler.getStackInSlot(0).setCount(sideItemHandler.getStackInSlot(0).getCount() - 1);
        if (sideItemHandler.getStackInSlot(0).getCount() == 0) sideItemHandler.setStackInSlot(0, ItemStack.EMPTY);

        this.saveChanges();

        //Exit early to avoid anything messing with our result
        cir.setReturnValue(hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP);

    }

    //This injection is needed to circumvent a check that literally just
    //return a null recipe if there is more than one item in an input slot
    //
    //Very similar code, just without that check
    @Inject(method = "getTask", at = @At("HEAD"), cancellable = true, remap = false)
    public void task(CallbackInfoReturnable<InscriberRecipe> cir) {

        if(((InscriberTileEntity) (Object) this).getInstalledUpgrades(Upgrades.CAPACITY) < 1) return;

        if (cachedTask != null || world == null) return;

        ItemStack input = sideItemHandler.getStackInSlot(0);
        ItemStack plateA = topItemHandler.getStackInSlot(0);
        ItemStack plateB = bottomItemHandler.getStackInSlot(0);

        if (input.isEmpty()) cir.setReturnValue(null);

        cachedTask = InscriberRecipes.findRecipe(this.world, input, plateA, plateB, true);

        cir.setReturnValue(cachedTask);
    }

}
