package com.eisiadev.enceladus.phcintegrated;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GardenUtil {

    private static final String NAMESPACE = "pamhc2crops";
    private static final String SUFFIX = "garden";
    private static final Map<Block, Boolean> CACHE = new ConcurrentHashMap<>();

    public static boolean isGarden(Block block) {
        Boolean cached = CACHE.get(block);
        if (cached != null) {
            return cached;
        }
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        boolean result = key != null && key.getNamespace().equals(NAMESPACE) && key.getPath().endsWith(SUFFIX);
        CACHE.put(block, result);
        return result;
    }

    public static boolean isArid(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key != null && key.getPath().startsWith("arid");
    }

    public static boolean isLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    private GardenUtil() {
    }
}
