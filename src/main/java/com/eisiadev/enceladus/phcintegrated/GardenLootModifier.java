package com.eisiadev.enceladus.phcintegrated;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class GardenLootModifier extends LootModifier {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PhcIntegrated.MOD_ID);

    public static final Supplier<Codec<GardenLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, GardenLootModifier::new)));

    static {
        SERIALIZERS.register("garden_drops", CODEC);
    }

    public GardenLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !GardenUtil.isGarden(state.getBlock())) {
            return generatedLoot;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (PhcConfig.SHEARS_DROP_BLOCK.get() && tool != null && tool.canPerformAction(ToolActions.SHEARS_HARVEST)) {
            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                ObjectArrayList<ItemStack> sheared = new ObjectArrayList<>();
                sheared.add(new ItemStack(item));
                return sheared;
            }
            return generatedLoot;
        }

        double multiplier = PhcConfig.DROP_MULTIPLIER.get();
        if (multiplier == 1.0D) {
            return generatedLoot;
        }

        RandomSource random = context.getRandom();
        ObjectArrayList<ItemStack> scaled = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            double exact = stack.getCount() * multiplier;
            int count = (int) Math.floor(exact);
            if (random.nextDouble() < exact - count) {
                count++;
            }
            while (count > 0) {
                int take = Math.min(count, stack.getMaxStackSize());
                ItemStack copy = stack.copy();
                copy.setCount(take);
                scaled.add(copy);
                count -= take;
            }
        }
        return scaled;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
