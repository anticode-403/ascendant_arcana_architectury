package me.anticode.ascendant_arcana.block;

import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingRestorineBlock extends Block {
    public static final int GROW_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingRestorineBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROW_CHANCE) == 0) {
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = world.getBlockState(blockPos);
            Block block = null;
            if (canGrowIn(blockState)) {
                block = AArcanaBlocks.SMALL_RESTORINE_BUD.get();
            } else if (blockState.is(AArcanaBlocks.SMALL_RESTORINE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = AArcanaBlocks.MEDIUM_RESTORINE_BUD.get();
            } else if (blockState.is(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = AArcanaBlocks.LARGE_RESTORINE_BUD.get();
            } else if (blockState.is(AArcanaBlocks.LARGE_RESTORINE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = AArcanaBlocks.RESTORINE_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockState2 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);
                world.setBlockAndUpdate(blockPos, blockState2);
            }

        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
