package com.example.mod;

import net.fabricmc.api.DedicatedServerModInitializer;
import revxrsal.commands.fabric.FabricLamp;

public class FabricTestMod implements DedicatedServerModInitializer {
    @Override public void onInitializeServer() {
        var lamp = FabricLamp.builder().build();
        lamp.register(new GreetCommand());
    }
}
