package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundWhirlwindSync(UUID playerId, boolean charging, boolean whirlwinding) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "whirlwind_client");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(playerId);
        buf.writeBoolean(charging);
        buf.writeBoolean(whirlwinding);
        return buf;
    }

    public static ClientboundWhirlwindSync read(FriendlyByteBuf buf) {
        return new ClientboundWhirlwindSync(buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }
}
