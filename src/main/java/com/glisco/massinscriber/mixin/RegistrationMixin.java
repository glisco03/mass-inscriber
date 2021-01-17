package com.glisco.massinscriber.mixin;

import appeng.api.config.Upgrades;
import appeng.core.Api;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "appeng.core.Registration")
public class RegistrationMixin {

    //Very simple again, just allow the capacity card in an inscriber
    @Inject(method = "postInit", at = @At("TAIL"),remap = false)
    private static void addCapacityToInscriber(CallbackInfo ci){
        Upgrades.CAPACITY.registerItem(Api.INSTANCE.definitions().blocks().inscriber(), 1);
    }

}
