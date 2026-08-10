package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonObject;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UniversalRepairRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final float repairAmount;
    private final boolean addition;

    public UniversalRepairRecipe(ResourceLocation id, Ingredient ingredient, float repairAmount, boolean addition) {
        this.id = id;
        this.ingredient = ingredient;
        this.repairAmount = repairAmount;
        this.addition = addition;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public boolean getAddition() {
        return addition;
    }

    public float getRepairAmount() {
        return repairAmount;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AArcanaRecipes.REPAIR_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AArcanaRecipes.REPAIR_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<UniversalRepairRecipe> {

        @Override
        public @NotNull UniversalRepairRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            Ingredient ingredient = Ingredient.fromJson(jsonObject.get("ingredient"));
            float repairAmount = jsonObject.get("repair_amount").getAsFloat();
            boolean addition = jsonObject.get("addition").getAsBoolean();
            return new UniversalRepairRecipe(resourceLocation, ingredient, repairAmount, addition);
        }

        @Override
        public UniversalRepairRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
            float repairAmount = friendlyByteBuf.readFloat();
            boolean addition = friendlyByteBuf.readBoolean();
            return new UniversalRepairRecipe(resourceLocation, ingredient, repairAmount, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, UniversalRepairRecipe recipe) {
            recipe.ingredient.toNetwork(friendlyByteBuf);
            friendlyByteBuf.writeFloat(recipe.repairAmount);
            friendlyByteBuf.writeBoolean(recipe.addition);
        }
    }
}
