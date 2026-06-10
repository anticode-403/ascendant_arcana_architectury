package me.anticode.ascendant_arcana.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record RestorineGrowthFeatureConfig(int floorToCeilingSearchRange, IntProvider width, FloatProvider radiusToHeightRatio, float ceilingPercentage, float restorine_percentage) implements FeatureConfiguration {
    public static final Codec<RestorineGrowthFeatureConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("search_range").forGetter(RestorineGrowthFeatureConfig::floorToCeilingSearchRange),
                    IntProvider.NON_NEGATIVE_CODEC.fieldOf("width").forGetter(RestorineGrowthFeatureConfig::width),
                    FloatProvider.CODEC.fieldOf("ratio").forGetter(RestorineGrowthFeatureConfig::radiusToHeightRatio),
                            ExtraCodecs.POSITIVE_FLOAT.fieldOf("stalagmite_percentage").forGetter(RestorineGrowthFeatureConfig::ceilingPercentage),
                            ExtraCodecs.POSITIVE_FLOAT.fieldOf("restorine_percentage").forGetter(RestorineGrowthFeatureConfig::restorine_percentage))
                .apply(instance, RestorineGrowthFeatureConfig::new));
}
