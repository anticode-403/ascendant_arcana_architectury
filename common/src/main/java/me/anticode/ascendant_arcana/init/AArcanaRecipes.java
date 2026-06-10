package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import me.anticode.ascendant_arcana.recipe.InfusionRecipe;
import me.anticode.ascendant_arcana.recipe.RelicCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class AArcanaRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.RECIPE_SERIALIZER);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.RECIPE_TYPE);

    public static RegistrySupplier<RecipeSerializer<InfusionRecipe>> INFUSION_RECIPE_SERIALIZER = register("infusion_smithing_recipe", new InfusionRecipe.Serializer());
    public static RegistrySupplier<RecipeSerializer<EnchantmentRecipe>> ENCHANTMENT_RECIPE_SERIALIZER = register("enchantment_recipe", new EnchantmentRecipe.Serializer());
    public static RegistrySupplier<RecipeSerializer<RelicCraftingRecipe>> RELIC_CRAFTING_RECIPE_SERIALIZER = register("relic_crafting_recipe", new RelicCraftingRecipe.Serializer());

    public static RegistrySupplier<RecipeType<EnchantmentRecipe>> ENCHANTMENT_RECIPE_TYPE = register("enchantment_recipe");

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> RegistrySupplier<S> register(String name, S serializer) {
        return RECIPE_SERIALIZERS.register(name, () -> serializer);
    }

    static <T extends Recipe<?>> RegistrySupplier<RecipeType<T>> register(final String id) {
        return RECIPE_TYPES.register(id, () -> new RecipeType<>() {
            public String toString() {
                return id;
            }
        });
    }

    public static void initialize() {
        RECIPE_TYPES.register();
        RECIPE_SERIALIZERS.register();
    }
}
