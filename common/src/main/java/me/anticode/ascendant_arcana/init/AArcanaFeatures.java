package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.registries.DeferredRegister;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.worldgen.feature.RestorineGrowthFeature;
import me.anticode.ascendant_arcana.worldgen.feature.RestorineGrowthFeatureConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;

public class AArcanaFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.FEATURE);

    public static final ResourceLocation RESTORINE_FEATURE_ID = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "restorine_growth");
    public static final ResourceLocation NETHERRACK_RESTORINE_FEATURE_ID = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "netherrack_restorine_growth");
    public static final RestorineGrowthFeature RESTORINE_FEATURE = new RestorineGrowthFeature(RestorineGrowthFeatureConfig.CODEC);

    public static void initialize() {
        FEATURES.register(RESTORINE_FEATURE_ID, () -> RESTORINE_FEATURE);
        FEATURES.register();

        BiomeModifications.addProperties((biomeContext, mutable) -> {
            if (biomeContext.hasTag(BiomeTags.IS_OVERWORLD) && !biomeContext.hasTag(BiomeTags.IS_OCEAN)) {
                mutable.getGenerationProperties().addFeature(
                        GenerationStep.Decoration.RAW_GENERATION,
                        ResourceKey.create(Registries.PLACED_FEATURE, RESTORINE_FEATURE_ID)
                );
            }
            if (biomeContext.hasTag(BiomeTags.IS_NETHER) && biomeContext.hasTag(BiomeTags.HAS_NETHER_FORTRESS)) {
                mutable.getGenerationProperties().addFeature(
                        GenerationStep.Decoration.RAW_GENERATION,
                        ResourceKey.create(Registries.PLACED_FEATURE, NETHERRACK_RESTORINE_FEATURE_ID)
                );
            }
        });
    }
}
