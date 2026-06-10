package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EnchantmentRecipe implements Recipe<Inventory> {
    private final ResourceLocation id;
    public final int magicalScrapCost;
    public final IngredientStack primaryIngredientStack;
    public final IngredientStack secondaryIngredientStack;
    public final int levelCost;
    public final Enchantment enchantment;

    EnchantmentRecipe(ResourceLocation id, int magicalScrapCost, IngredientStack primaryIngredientStack, IngredientStack secondaryIngredientStack, int levelCost, Enchantment enchantment) {
        this.id = id;
        this.magicalScrapCost = magicalScrapCost;
        this.primaryIngredientStack = primaryIngredientStack;
        this.secondaryIngredientStack = secondaryIngredientStack;
        this.levelCost = levelCost;
        this.enchantment = enchantment;
    }

    @Override
    public boolean matches(Inventory inventory, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(Inventory inventory, RegistryAccess registryManager) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryManager) {
        return null;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AArcanaRecipes.ENCHANTMENT_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AArcanaRecipes.ENCHANTMENT_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<EnchantmentRecipe> {

        @Override
        public @NotNull EnchantmentRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonElement magicalScrapCostJson = json.get("magical_scrap_cost");
            int magicalScrapCost = 3;
            if (!magicalScrapCostJson.isJsonNull()) magicalScrapCost = magicalScrapCostJson.getAsInt();
            IngredientStack primaryIngredientStack = IngredientStack.fromJson((JsonObject) json.get("primary_ingredient"));
            IngredientStack secondaryIngredientStack = IngredientStack.fromJson((JsonObject) json.get("secondary_ingredient"));
            JsonElement levelCostJson = json.get("level_cost");
            int levelCost = 3;
            if (levelCostJson != null && !levelCostJson.isJsonNull()) levelCost = levelCostJson.getAsInt();
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(ResourceLocation.tryParse(json.get("enchantment").getAsString())).orElse(null);
            if (enchantment == null) {
                throw new JsonParseException("Enchantment not found");
            }
            return new EnchantmentRecipe(id, magicalScrapCost, primaryIngredientStack, secondaryIngredientStack, levelCost, enchantment);
        }

        @Override
        public @NotNull EnchantmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int magicalScrapCost = buf.readInt();
            IngredientStack primaryIngredientStack = null;
            if (buf.readBoolean()) {
                primaryIngredientStack = IngredientStack.fromNetwork(buf);
            }
            IngredientStack secondaryIngredientStack = null;
            if (buf.readBoolean()) {
                secondaryIngredientStack = IngredientStack.fromNetwork(buf);
            }
            int levelCost = buf.readInt();
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(ResourceLocation.tryParse(buf.readUtf())).orElse(null);
            return new EnchantmentRecipe(id, magicalScrapCost, primaryIngredientStack, secondaryIngredientStack, levelCost, enchantment);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, EnchantmentRecipe recipe) {
            buf.writeInt(recipe.magicalScrapCost);
            buf.writeBoolean(recipe.primaryIngredientStack != null);
            if (recipe.primaryIngredientStack != null) {
                recipe.primaryIngredientStack.toNetwork(buf);
            }
            buf.writeBoolean(recipe.secondaryIngredientStack != null);
            if (recipe.secondaryIngredientStack != null) {
                recipe.secondaryIngredientStack.toNetwork(buf);
            }
            buf.writeInt(recipe.levelCost);
            buf.writeUtf(BuiltInRegistries.ENCHANTMENT.getKey(recipe.enchantment).toString());
        }
    }
}
