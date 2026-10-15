package com.eisiadev.enceladus.phcintegrated;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PhcConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue SHEARS_DROP_BLOCK;
    public static final ForgeConfigSpec.DoubleValue DROP_MULTIPLIER;

    public static final ForgeConfigSpec.BooleanValue SPREAD_ENABLED;
    public static final ForgeConfigSpec.IntValue SPREAD_INTERVAL_TICKS;
    public static final ForgeConfigSpec.DoubleValue SPREAD_CHANCE;
    public static final ForgeConfigSpec.IntValue SPREAD_RANGE;
    public static final ForgeConfigSpec.IntValue SCAN_CHUNK_RADIUS;
    public static final ForgeConfigSpec.IntValue DENSITY_RADIUS;
    public static final ForgeConfigSpec.IntValue MAX_GARDENS_IN_DENSITY_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("harvest");
        SHEARS_DROP_BLOCK = builder
                .comment("Breaking a garden with shears drops the garden block itself.")
                .define("shearsDropBlock", true);
        DROP_MULTIPLIER = builder
                .comment("Multiplier applied to crop drops of gardens. 0.5 halves, 2.0 doubles. Fractions are rolled randomly.")
                .defineInRange("dropMultiplier", 1.0D, 0.0D, 64.0D);
        builder.pop();

        builder.push("spread");
        SPREAD_ENABLED = builder
                .comment("Enable natural garden spreading.")
                .define("enabled", true);
        SPREAD_INTERVAL_TICKS = builder
                .comment("Ticks between spread cycles. 20 ticks = 1 second.")
                .defineInRange("intervalTicks", 1200, 20, 1728000);
        SPREAD_CHANCE = builder
                .comment("Chance per garden to attempt a spread each cycle.")
                .defineInRange("chance", 0.2D, 0.0D, 1.0D);
        SPREAD_RANGE = builder
                .comment("Maximum horizontal distance in blocks a new garden can appear from its parent.")
                .defineInRange("range", 3, 1, 16);
        SCAN_CHUNK_RADIUS = builder
                .comment("Chunk radius around each player in which gardens are able to spread.")
                .defineInRange("scanChunkRadius", 4, 1, 16);
        DENSITY_RADIUS = builder
                .comment("Radius used to count nearby gardens before spreading.")
                .defineInRange("densityRadius", 4, 1, 8);
        MAX_GARDENS_IN_DENSITY_RADIUS = builder
                .comment("Spreading stops when this many gardens already exist within densityRadius of the target.")
                .defineInRange("maxGardensInDensityRadius", 4, 1, 4096);
        builder.pop();

        SPEC = builder.build();
    }

    private PhcConfig() {
    }
}
