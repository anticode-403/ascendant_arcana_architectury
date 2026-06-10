package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

public record EnchantingScreenSendRecipe(int syncId, EnchantmentRecipe recipe) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "enchanting_screen_send_recipe");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(syncId);
        buf.writeUtf(recipe.getId().toString());
        return buf;
    }

    public static EnchantingScreenSendRecipe read(FriendlyByteBuf buf, RecipeManager recipeManager) {
        int syncId = buf.readInt();
        String id = buf.readUtf();
        EnchantmentRecipe recipe = (EnchantmentRecipe) recipeManager.byKey(ResourceLocation.tryParse(id)).get();
        return new EnchantingScreenSendRecipe(syncId, recipe);
    }
}
