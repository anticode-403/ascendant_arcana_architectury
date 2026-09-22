package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RelicCraftingRecipe extends CustomRecipe {
    final String group;
    final int strength;
    final ResourceLocation relic;
    final NonNullList<Ingredient> input;

    public RelicCraftingRecipe(ResourceLocation id, String group, CraftingBookCategory category, int strength, ResourceLocation relic, NonNullList<Ingredient> input) {
        super(id, category);
        this.group = group;
        this.strength = strength;
        this.relic = relic;
        this.input = input;

    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        int i = 0;
        Map<Item, Integer> inputs = new HashMap<>();

        for (int j = 0; j < inventory.getContainerSize(); ++j) {
            ItemStack itemStack = inventory.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                for (Ingredient ingredient : input) {
                    if (ingredient.test(itemStack)) {
                        Item item = itemStack.getItem();
                        if (itemStack.is(AArcanaItems.RELIC.get())) {
                            if (RelicItem.getRelicType(itemStack) == RelicRegistry.get(relic) && RelicItem.getRelicStrength(itemStack) == strength - 1) {
                                if (inputs.containsKey(item)) {
                                    inputs.put(item, inputs.get(item) + 1);
                                } else inputs.put(item, 1);
                            }
                        } else {
                            if (inputs.containsKey(item)) {
                                inputs.put(item, inputs.get(item) + 1);
                            } else inputs.put(item, 1);
                        }
                        break;
                    }
                }
            }
        }
        if (i == input.size()) {
            boolean matches = true;
            Map<Item, Integer> countedIngredients = new HashMap<>();
            for (Ingredient ingredient : input) {
                Item item = ingredient.getItems()[0].getItem();
                if (countedIngredients.containsKey(item)) {
                    countedIngredients.put(item, countedIngredients.get(item) + 1);
                } else countedIngredients.put(item, 1);
            }
            for (Map.Entry<Item, Integer> entry : countedIngredients.entrySet()) {
                boolean matchesAny = false;
                for (Map.Entry<Item, Integer> item : inputs.entrySet()) {
                    if (entry.getKey() == item.getKey()) {
                        if (item.getValue() == entry.getValue()) {
                            matchesAny = true;
                            break;
                        }
                    }
                }
                if (!matchesAny) {
                    matches = false;
                    break;
                }
            }
            return matches;
        }
        return false;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return input;
    }

    public ItemStack getOutput() {
        ItemStack itemStack = new ItemStack(AArcanaItems.RELIC.get());
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putInt(RelicItem.RELIC_STRENGTH_KEY, strength);
        nbt.putString(RelicItem.RELIC_TYPE_KEY, relic.toString());
        return itemStack;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryManager) {
        return getOutput();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inventory, RegistryAccess registryManager) {
        return getOutput();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= input.size();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AArcanaRecipes.RELIC_CRAFTING_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<RelicCraftingRecipe> {
        @Override
        public @NotNull RelicCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            int strength = GsonHelper.getAsInt(json, "strength", 0);
            String relic = GsonHelper.getAsString(json, "relic", "");
            CraftingBookCategory craftingRecipeCategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            NonNullList<Ingredient> defaultedList = getIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (defaultedList.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (defaultedList.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                return new RelicCraftingRecipe(id, group, craftingRecipeCategory, strength, ResourceLocation.tryParse(relic), defaultedList);
            }
        }

        private static NonNullList<Ingredient> getIngredients(JsonArray json) {
            NonNullList<Ingredient> defaultedList = NonNullList.create();

            for(int i = 0; i < json.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(json.get(i), false);
                if (!ingredient.isEmpty()) {
                    defaultedList.add(ingredient);
                }
            }

            return defaultedList;
        }

        @Override
        public @NotNull RelicCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            int strength = buf.readInt();
            ResourceLocation relic = buf.readResourceLocation();
            int i = buf.readVarInt();

            NonNullList<Ingredient> list = NonNullList.withSize(i, Ingredient.EMPTY);
            list.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            return new RelicCraftingRecipe(id, group, category, strength, relic, list);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, RelicCraftingRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category());
            buf.writeInt(recipe.strength);
            buf.writeResourceLocation(recipe.relic);
            buf.writeVarInt(recipe.input.size());

            for (Ingredient ingredient : recipe.input) {
                ingredient.toNetwork(buf);
            }
        }
    }
}
