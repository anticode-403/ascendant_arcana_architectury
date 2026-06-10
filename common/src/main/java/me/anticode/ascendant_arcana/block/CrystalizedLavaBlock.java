package me.anticode.ascendant_arcana.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;


public class CrystalizedLavaBlock extends FrostedIceBlock {

    public CrystalizedLavaBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    public static BlockState getLavaState() {
        return Blocks.LAVA.defaultBlockState();
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
        world.setBlock(pos, getLavaState(), 0);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if ((random.nextInt(5) == 0 && canMelt(level, pos, 4)) && increaseAge(state, level, pos)) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for(Direction direction : Direction.values()) {
                mutable.setWithOffset(pos, direction);
                BlockState blockState = level.getBlockState(mutable);
                if (blockState.is(this) && !increaseAge(blockState, level, mutable)) {
                    level.scheduleTick(mutable, this, Mth.nextInt(random, 20, 40));
                }
            }

        } else {
            level.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
        }
    }

    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, getLavaState());
        level.updateNeighborsAt(pos, getLavaState().getBlock());
    }

    private boolean increaseAge(BlockState state, Level level, BlockPos pos) {
        int i = state.getValue(AGE);
        if (i < MAX_AGE) {
            level.setBlockAndUpdate(pos, state.setValue(AGE, i + 1));
            return false;
        } else {
            this.melt(state, level, pos);
            return true;
        }
    }

    private boolean canMelt(Level level, BlockPos pos, int maxNeighbors) {
        int i = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            if (level.getBlockState(mutable).is(this)) {
                ++i;
                if (i >= maxNeighbors) {
                    return false;
                }
            }
        }

        return true;
    }
}
