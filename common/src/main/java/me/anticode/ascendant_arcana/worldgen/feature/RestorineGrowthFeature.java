package me.anticode.ascendant_arcana.worldgen.feature;

import com.mojang.serialization.Codec;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestorineGrowthFeature extends Feature<RestorineGrowthFeatureConfig> {
    public RestorineGrowthFeature(Codec<RestorineGrowthFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RestorineGrowthFeatureConfig> context) {

        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        RestorineGrowthFeatureConfig config = context.config();

        Optional<Column> optional = Column.scan(level, pos, config.floorToCeilingSearchRange(), RestorineGrowthFeature::canGenerate, RestorineGrowthFeature::canReplaceOrLava);
        if (optional.isPresent() && !(optional.get() instanceof Column.Line)) {
            Column column = optional.get();
            int width = config.width().sample(random);
            int heightScale = (int)(config.radiusToHeightRatio().sample(random) * (float)width);
            if (column.getHeight().isPresent() && column.getHeight().getAsInt() < heightScale) return false;
            boolean isStalagmite = random.nextFloat() < config.ceilingPercentage();
            if (column instanceof Column.Ray && column.getCeiling().isPresent()) isStalagmite = true;
            else if (column instanceof Column.Ray && column.getFloor().isPresent()) isStalagmite = false;

            int y = isStalagmite ? column.getCeiling().getAsInt() : column.getFloor().getAsInt();
            RestorineGrowthGenerator generator = new RestorineGrowthGenerator(pos.atY(y), isStalagmite, width, 0.1, heightScale, config.netherrack());
            if (!generator.canGenerate(level)) return false;

            generator.generate(level, random);
        }
        return false;
    }

    private static boolean canGenerate(BlockState blockState) {
        return blockState.isAir();
    }

    private static boolean canReplaceOrLava(BlockState state) {
        return state.is(BlockTags.DRIPSTONE_REPLACEABLE) || state.is(BlockTags.BASE_STONE_NETHER) || state.is(Blocks.LAVA);
    }

    static final class RestorineGrowthGenerator {
        private BlockPos pos;
        private final boolean isStalagmite;
        private int scale;
        private final double bluntness;
        private final double heightScale;
        private final boolean netherrack;

        RestorineGrowthGenerator(BlockPos pos, boolean isStalagmite, int scale, double bluntness, double heightScale, boolean netherrack) {
            this.pos = pos;
            this.isStalagmite = isStalagmite;
            this.scale = scale;
            this.bluntness = bluntness;
            this.heightScale = heightScale;
            this.netherrack = netherrack;
        }

        private int getBaseScale() {
            return this.scale(0.0F);
        }

        boolean canGenerate(WorldGenLevel world) {
            while(this.scale > 1) {
                BlockPos.MutableBlockPos mutable = this.pos.mutable();
                int i = Math.min(10, this.getBaseScale());

                for(int j = 0; j < i; ++j) {
                    if (world.getBlockState(mutable).is(Blocks.LAVA)) {
                        return false;
                    }

                    if (canGenerateBase(world, mutable, this.scale)) {
                        this.pos = mutable;
                        return true;
                    }

                    mutable.move(this.isStalagmite ? Direction.DOWN : Direction.UP);
                }

                this.scale /= 2;
            }

            return false;
        }

        static boolean canGenerateBase(WorldGenLevel level, BlockPos pos, int height) {
            if (canGenerateOrLava(level, pos)) {
                return false;
            } else {
                float g = 6.0F / (float)height;

                for(float h = 0.0F; h < ((float)Math.PI * 2F); h += g) {
                    int i = (int)(Mth.cos(h) * (float)height);
                    int j = (int)(Mth.sin(h) * (float)height);
                    if (canGenerateOrLava(level, pos.offset(i, 0, j))) {
                        return false;
                    }
                }

                return true;
            }
        }

        static boolean canGenerateOrLava(WorldGenLevel level, BlockPos pos) {
            return level.isStateAtPosition(pos, RestorineGrowthGenerator::canGenerateOrLava);
        }

        public static boolean canGenerateOrLava(BlockState state) {
            return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
        }

        private int scale(float height) {
            return (int)scaleHeightFromRadius(height, this.scale, this.heightScale, this.bluntness);
        }

        static double scaleHeightFromRadius(double radius, double scale, double heightScale, double bluntness) {
            if (radius < bluntness) {
                radius = bluntness;
            }

            double e = radius / scale * 0.384;
            double f = (double)0.75F * Math.pow(e, 1.3333333333333333);
            double g = Math.pow(e, 0.6666666666666666);
            double h = 0.3333333333333333 * Math.log(e);
            double i = heightScale * (f - g - h);
            i = Math.max(i, 0.0F);
            return i / 0.384 * scale;
        }

        void generate(WorldGenLevel level, RandomSource random) {
            List<BlockPos> budding_restorine = new ArrayList<>();
            for(int i = -this.scale; i <= this.scale; ++i) {
                for(int j = -this.scale; j <= this.scale; ++j) {
                    float f = Mth.sqrt((float)(i * i + j * j));
                    if (!(f > (float)this.scale)) {
                        int k = this.scale(f);
                        if (k > 0) {
                            if ((double)random.nextFloat() < 0.2) {
                                k = (int)((float)k * Mth.nextFloat(random, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos mutable = this.pos.offset(i, 0, j).mutable();
                            boolean bl = false;
                            int l = this.isStalagmite ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mutable.getX(), mutable.getZ()) : Integer.MAX_VALUE;

                            for(int m = 0; m < k && mutable.getY() < l; ++m) {
                                if (canGenerateOrLava(level, mutable)) {
                                    bl = true;
                                    Block block = netherrack ? Blocks.NETHERRACK : Blocks.STONE;
                                    if (random.nextFloat() < 0.2F) {
                                        block = netherrack ? AArcanaBlocks.NETHERRACK_BUDDING_RESTORINE.get() : AArcanaBlocks.BUDDING_RESTORINE.get();
                                        budding_restorine.add(mutable.mutable());
                                    }
                                    level.setBlock(mutable, block.defaultBlockState(), 2);
                                } else if (bl && level.getBlockState(mutable).is(BlockTags.BASE_STONE_OVERWORLD)) {
                                    break;
                                }

                                mutable.move(this.isStalagmite ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }
            for (BlockPos pos : budding_restorine) {
                for (int l = 0; l < 6; l++) {
                    Block block = switch (random.nextInt(0, 13)) {
                        case 0 -> AArcanaBlocks.SMALL_RESTORINE_BUD.get();
                        case 1 -> AArcanaBlocks.MEDIUM_RESTORINE_BUD.get();
                        case 2 -> AArcanaBlocks.LARGE_RESTORINE_BUD.get();
                        case 3, 4, 5, 6 -> AArcanaBlocks.RESTORINE_CLUSTER.get();
                        case 7, 8, 9, 10 -> AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get();
                        default -> Blocks.AIR;
                    };
                    if (block == Blocks.AIR) continue;
                    BlockPos.MutableBlockPos mutable = pos.mutable();
                    Direction[] directions = Direction.values();
                    BlockPos restorineBud = mutable.mutable().offset(directions[l].getNormal());
                    if (level.getBlockState(restorineBud).isAir()) {
                        level.setBlock(restorineBud, block.defaultBlockState().setValue(AmethystClusterBlock.FACING, directions[l]).setValue(AmethystClusterBlock.WATERLOGGED, level.getBlockState(restorineBud).getFluidState().getType() == Fluids.WATER), 2);
                    }
                }
            }
        }
    }
}
