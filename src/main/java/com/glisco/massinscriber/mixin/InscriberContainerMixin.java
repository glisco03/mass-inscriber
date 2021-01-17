package com.glisco.massinscriber.mixin;

import appeng.api.config.Upgrades;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.InscriberContainer;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.misc.InscriberTileEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InscriberContainer.class)
public class InscriberContainerMixin extends AEBaseContainer {

    @Shadow
    @Final
    private InscriberTileEntity ti;

    @Shadow
    @Final
    private Slot top;

    @Shadow
    @Final
    private Slot middle;

    @Shadow
    @Final
    private Slot bottom;

    public InscriberContainerMixin(ContainerType<?> containerType, int id, PlayerInventory ip, TileEntity myTile, IPart myPart) {
        super(containerType, id, ip, myTile, myPart);
    }

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void construct(int id, PlayerInventory ip, InscriberTileEntity te, CallbackInfo ci, IItemHandler inv, RestrictedInputSlot top, RestrictedInputSlot bottom, RestrictedInputSlot middle) {
        if (te.getInstalledUpgrades(Upgrades.CAPACITY) > 0) {
            top.setStackLimit(64);
            bottom.setStackLimit(64);
            middle.setStackLimit(64);
        }
    }

    // ----
    //
    //Warning: Dirty, lazy code ahead
    //
    // ----

    //The gui acts weird if I just set the slot limits here
    //
    //so..
    //
    //I just close it. hehe
    //And also drop excess items in input slots if an upgrade was removed
    //
    //Also this is probably not the right place to do this, but it works
    //And please ignore the searge function name
    //Mixin is complicated, ok?
    @Inject(method = "func_75142_b", at = @At("TAIL"), remap = false)
    public void detectUpgradeChange(CallbackInfo ci) {
        if (this.ti.getInstalledUpgrades(Upgrades.CAPACITY) > 0 && this.top.getSlotStackLimit() == 1) {
            this.setValidContainer(false);
        } else if (this.ti.getInstalledUpgrades(Upgrades.CAPACITY) == 0 && this.top.getSlotStackLimit() == 64) {
            if (this.top.getStack().getCount() > 1) {
                ItemStack toDrop = this.top.getStack().copy();
                toDrop.setCount(toDrop.getCount() - 1);

                this.ti.getWorld().addEntity(new ItemEntity(this.ti.getWorld(), this.ti.getPos().getX(), this.ti.getPos().getY(), this.ti.getPos().getZ(), toDrop));
                this.top.getStack().setCount(1);
            }

            if (this.middle.getStack().getCount() > 1) {
                ItemStack toDrop = this.middle.getStack().copy();
                toDrop.setCount(toDrop.getCount() - 1);

                this.ti.getWorld().addEntity(new ItemEntity(this.ti.getWorld(), this.ti.getPos().getX(), this.ti.getPos().getY(), this.ti.getPos().getZ(), toDrop));
                this.middle.getStack().setCount(1);
            }

            if (this.bottom.getStack().getCount() > 1) {
                ItemStack toDrop = this.bottom.getStack().copy();
                toDrop.setCount(toDrop.getCount() - 1);

                this.ti.getWorld().addEntity(new ItemEntity(this.ti.getWorld(), this.ti.getPos().getX(), this.ti.getPos().getY(), this.ti.getPos().getZ(), toDrop));
                this.bottom.getStack().setCount(1);
            }

            this.setValidContainer(false);
        }
    }

}
