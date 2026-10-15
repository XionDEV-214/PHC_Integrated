package com.eisiadev.enceladus.phcintegrated;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PhcIntegrated.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GardenSpreadHandler {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!PhcConfig.SPREAD_ENABLED.get()) {
            return;
        }
        if (level.getGameTime() % PhcConfig.SPREAD_INTERVAL_TICKS.get() != 0L) {
            return;
        }
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }

        int radius = PhcConfig.SCAN_CHUNK_RADIUS.get();
        LongOpenHashSet visited = new LongOpenHashSet();
        List<BlockPos> gardens = new ArrayList<>();

        for (ServerPlayer player : players) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int chunkX = center.x + dx;
                    int chunkZ = center.z + dz;
                    if (!visited.add(ChunkPos.asLong(chunkX, chunkZ))) {
                        continue;
                    }
                    LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                    if (chunk != null) {
                        collectGardens(chunk, gardens);
                    }
                }
            }
        }

        for (BlockPos pos : gardens) {
            trySpread(level, pos);
        }
    }

    private static void collectGardens(LevelChunk chunk, List<BlockPos> out) {
        LevelChunkSection[] sections = chunk.getSections();
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section.hasOnlyAir() || !section.maybeHas(state -> GardenUtil.isGarden(state.getBlock()))) {
                continue;
            }
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (GardenUtil.isGarden(section.getBlockState(x, y, z).getBlock())) {
                            out.add(new BlockPos(baseX + x, baseY + y, baseZ + z));
                        }
                    }
                }
            }
        }
    }

    private static void trySpread(ServerLevel level, BlockPos origin) {
        RandomSource random = level.random;
        if (random.nextDouble() >= PhcConfig.SPREAD_CHANCE.get()) {
            return;
        }
        BlockState source = level.getBlockState(origin);
        if (!GardenUtil.isGarden(source.getBlock())) {
            return;
        }

        int range = PhcConfig.SPREAD_RANGE.get();
        BlockPos target = origin.offset(
                random.nextInt(range * 2 + 1) - range,
                random.nextInt(3) - 1,
                random.nextInt(range * 2 + 1) - range);
        if (target.equals(origin) || !GardenUtil.isLoaded(level, target)) {
            return;
        }
        if (!level.getBlockState(target).isAir()) {
            return;
        }

        BlockState placed = source.getBlock().defaultBlockState();
        if (!canStand(level, target, placed)) {
            return;
        }
        if (countNearby(level, target) >= PhcConfig.MAX_GARDENS_IN_DENSITY_RADIUS.get()) {
            return;
        }
        level.setBlock(target, placed, Block.UPDATE_ALL);
    }

    private static boolean canStand(ServerLevel level, BlockPos target, BlockState placed) {
        if (placed.canSurvive(level, target)) {
            return true;
        }
        if (GardenUtil.isArid(placed.getBlock())) {
            BlockState below = level.getBlockState(target.below());
            return below.is(BlockTags.SAND) || below.is(BlockTags.TERRACOTTA) || below.is(BlockTags.DIRT);
        }
        return false;
    }

    private static int countNearby(ServerLevel level, BlockPos center) {
        int radius = PhcConfig.DENSITY_RADIUS.get();
        int limit = PhcConfig.MAX_GARDENS_IN_DENSITY_RADIUS.get();
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!GardenUtil.isLoaded(level, cursor)) {
                        continue;
                    }
                    if (GardenUtil.isGarden(level.getBlockState(cursor).getBlock())) {
                        count++;
                        if (count >= limit) {
                            return count;
                        }
                    }
                }
            }
        }
        return count;
    }

    private GardenSpreadHandler() {
    }
}
