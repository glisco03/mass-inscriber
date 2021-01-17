package com.glisco.massinscriber;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

//Forge, please, just let me have a pure mixin mod
//
//Maybe that's possible and I'm just not aware
//Who knows
@Mod("mass-inscriber")
public class MassInscriber {

    public MassInscriber() {
        MinecraftForge.EVENT_BUS.register(this);
    }

}
