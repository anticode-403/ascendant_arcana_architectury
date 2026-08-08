package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonArray;
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

import java.util.ArrayList;
import java.util.List;

public class EnchantmentRecipe implements Recipe<Inventory> {
    private final ResourceLocation id;
    private final List<EnchantmentLevelRecipe> levels;
    public final Enchantment enchantment;

    EnchantmentRecipe(ResourceLocation id, List<EnchantmentLevelRecipe> levels, Enchantment enchantment) {
        this.id = id;
        this.levels = levels;
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

    public @NotNull List<EnchantmentLevelRecipe> getLevels() {
        return levels;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AArcanaRecipes.ENCHANTMENT_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AArcanaRecipes.ENCHANTMENT_RECIPE_TYPE.get();
    }

    public record EnchantmentLevelRecipe(IngredientStack scrapStack, IngredientStack primaryIngredientStack,
                                         IngredientStack secondaryIngredientStack, int levelCost) {

        public static @NotNull EnchantmentLevelRecipe fromJson(JsonObject json) {
            IngredientStack magicalStack = IngredientStack.fromJson((JsonObject) json.get("scrap_stack"));
            IngredientStack primaryIngredientStack = IngredientStack.fromJson((JsonObject) json.get("primary_ingredient"));
            IngredientStack secondaryIngredientStack = IngredientStack.fromJson((JsonObject) json.get("secondary_ingredient"));
            JsonElement levelCostJson = json.get("level_cost");
            int levelCost = 3;
            if (levelCostJson != null && !levelCostJson.isJsonNull()) levelCost = levelCostJson.getAsInt();
            return new EnchantmentLevelRecipe(magicalStack, primaryIngredientStack, secondaryIngredientStack, levelCost);
        }

        public static @NotNull EnchantmentLevelRecipe fromNetwork(FriendlyByteBuf buf) {
            IngredientStack magicalStack = null;
            if (buf.readBoolean()) {
                magicalStack = IngredientStack.fromNetwork(buf);
            }
            IngredientStack primaryIngredientStack = null;
            if (buf.readBoolean()) {
                primaryIngredientStack = IngredientStack.fromNetwork(buf);
            }
            IngredientStack secondaryIngredientStack = null;
            if (buf.readBoolean()) {
                secondaryIngredientStack = IngredientStack.fromNetwork(buf);
            }
            int levelCost = buf.readInt();
            return new EnchantmentLevelRecipe(magicalStack, primaryIngredientStack, secondaryIngredientStack, levelCost);
        }

        public static void toNetwork(EnchantmentLevelRecipe recipe, FriendlyByteBuf buf) {
            buf.writeBoolean(recipe.scrapStack != null);
            if (recipe.scrapStack != null) {
                recipe.scrapStack.toNetwork(buf);
            }
            buf.writeBoolean(recipe.primaryIngredientStack != null);
            if (recipe.primaryIngredientStack != null) {
                recipe.primaryIngredientStack.toNetwork(buf);
            }
            buf.writeBoolean(recipe.secondaryIngredientStack != null);
            if (recipe.secondaryIngredientStack != null) {
                recipe.secondaryIngredientStack.toNetwork(buf);
            }
            buf.writeInt(recipe.levelCost);
        }
    }

    public static class Serializer implements RecipeSerializer<EnchantmentRecipe> {

        @Override
        public @NotNull EnchantmentRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray levels = json.getAsJsonArray("levels");
            List<EnchantmentLevelRecipe> list = new ArrayList<>();
            levels.forEach(level -> list.add(EnchantmentLevelRecipe.fromJson(level.getAsJsonObject())));
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(ResourceLocation.tryParse(json.get("enchantment").getAsString())).orElse(null);
            if (enchantment == null) {
                throw new JsonParseException("Enchantment not found");
            }
            return new EnchantmentRecipe(id, list, enchantment);
        }

        @Override
        public @NotNull EnchantmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(ResourceLocation.tryParse(buf.readUtf())).orElse(null);
            int levelCount = buf.readInt();
            List<EnchantmentLevelRecipe> list = new ArrayList<>();
            for (int i = 0; i < levelCount; i++) {
                list.add(EnchantmentLevelRecipe.fromNetwork(buf));
            }
            return new EnchantmentRecipe(id, list, enchantment);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, EnchantmentRecipe recipe) {
            buf.writeUtf(BuiltInRegistries.ENCHANTMENT.getKey(recipe.enchantment).toString());
            buf.writeInt(recipe.levels.size());
            for (EnchantmentLevelRecipe level : recipe.levels) {
                EnchantmentLevelRecipe.toNetwork(level, buf);
            }
        }
    }
}
