package me.anticode.ascendant_arcana.networking;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public record EnchantingScreenSync(int syncId, List<Enchantment> treasures) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "enchanting_screen_sync");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(syncId);
        buf.writeInt(treasures.size());
        for (Enchantment enchantment : treasures) {
            buf.writeUtf(BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString());
        }
        return buf;
    }

    public static EnchantingScreenSync read(FriendlyByteBuf buf) {
        int syncId = buf.readInt();
        int i = buf.readInt();
        List<Enchantment> treasures = Lists.newArrayList();
        for (int j = 0; j < i; ++j) {
            ResourceLocation enchantId = ResourceLocation.tryParse(buf.readUtf());
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(enchantId);
            treasures.add(enchantment);
        }
        return new EnchantingScreenSync(syncId, treasures);
    }
}
