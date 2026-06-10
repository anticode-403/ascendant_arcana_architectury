package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record EnchantingScreenRemoveRecipe(int syncId) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "enchanting_screen_remove_recipe");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(syncId);
        return buf;
    }

    public static EnchantingScreenRemoveRecipe read(FriendlyByteBuf buf) {
        int syncId = buf.readInt();
        return new EnchantingScreenRemoveRecipe(syncId);
    }
}
