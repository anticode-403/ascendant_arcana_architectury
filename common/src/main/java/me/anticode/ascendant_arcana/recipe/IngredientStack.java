package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Predicate;

public class IngredientStack implements Predicate<ItemStack> {
    private final Ingredient ingredient;
    private final int count;

    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return ingredient.test(itemStack) && count <= itemStack.getCount();
    }

    public void toNetwork(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeInt(count);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("ingredient", ingredient.toJson());
        json.addProperty("count", count);
        return json;
    }

    public static IngredientStack of(ItemLike item) {
        return new IngredientStack(Ingredient.of(item), 1);
    }

    public static IngredientStack of(TagKey<Item> tag) {
        return new IngredientStack(Ingredient.of(tag), 1);
    }

    public static IngredientStack of(ItemLike item, int count) {
        return new IngredientStack(Ingredient.of(item), count);
    }

    public static IngredientStack of(TagKey<Item> tag, int count) {
        return new IngredientStack(Ingredient.of(tag), count);
    }

    public static IngredientStack fromNetwork(FriendlyByteBuf buf) {
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        int count = buf.readInt();
        return new IngredientStack(ingredient, count);
    }

    public static IngredientStack fromJson (JsonObject json) {
        if (json == null || json.isJsonNull()) return null;
        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
        int count = json.get("count").getAsInt();
        return new IngredientStack(ingredient, count);
    }
}
