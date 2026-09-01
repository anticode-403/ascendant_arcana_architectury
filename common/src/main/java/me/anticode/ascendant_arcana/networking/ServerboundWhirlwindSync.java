package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ServerboundWhirlwindSync(boolean charging, boolean whirlwinding) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "whirlwind_server");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(charging);
        buf.writeBoolean(whirlwinding);
        return buf;
    }

    public static ServerboundWhirlwindSync read(FriendlyByteBuf buf) {
        return new ServerboundWhirlwindSync(buf.readBoolean(), buf.readBoolean());
    }
}
