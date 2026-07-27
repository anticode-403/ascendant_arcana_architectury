package me.anticode.ascendant_arcana.worldgen.feature;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public record RestorineGrowthFeatureConfig(IntProvider baseHeight, IntProvider heightOffset, IntProvider spreadOffset, IntProvider count, BlockStateProvider coreBlock, BlockStateProvider depthBlock, BlockStateProvider alternateCoreBlock, float alternateChance, List<BlockState> innerPlacements, float innerPlacementChance, BlockStateProvider fillBlock, BlockPredicate target) implements FeatureConfiguration
{
    public static final Codec<RestorineGrowthFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            IntProvider.CODEC.fieldOf("height").forGetter(c -> c.baseHeight),
                            IntProvider.CODEC.fieldOf("height_offset").forGetter(c -> c.heightOffset),
                            IntProvider.CODEC.fieldOf("spread_offset").forGetter(c -> c.spreadOffset),
                            IntProvider.CODEC.fieldOf("count").forGetter(c -> c.count),
                            BlockStateProvider.CODEC.fieldOf("base_block").forGetter(c -> c.coreBlock),
                            BlockStateProvider.CODEC.fieldOf("depth_block").forGetter(c -> c.depthBlock),
                            BlockStateProvider.CODEC.fieldOf("alternate_base_block").forGetter(c -> c.alternateCoreBlock),
                            Codec.FLOAT.fieldOf("alternate_chance").forGetter(c->c.alternateChance),
                            ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(c -> c.innerPlacements),
                            Codec.FLOAT.fieldOf("inner_placement_chance").forGetter(c->c.innerPlacementChance),
                            BlockStateProvider.CODEC.fieldOf("fill_block").forGetter(c -> c.fillBlock),
                            BlockPredicate.CODEC.fieldOf("inner_target").forGetter(c->c.target)
                    )
                    .apply(instance, RestorineGrowthFeatureConfig::new));
}