package com.eisiadev.enceladus.phcintegrated;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PhcIntegrated.MOD_ID)
public class PhcIntegrated {

    public static final String MOD_ID = "phcintegrated";

    public PhcIntegrated(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, PhcConfig.SPEC, MOD_ID + "-common.toml");
        GardenLootModifier.SERIALIZERS.register(modBus);
    }
}
