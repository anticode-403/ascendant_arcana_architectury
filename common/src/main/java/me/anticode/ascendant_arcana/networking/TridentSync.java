package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record TridentSync(int tridentEntityId, int stuckEntityId) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "trident_sync");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(tridentEntityId);
        buf.writeInt(stuckEntityId);
        return buf;
    }

    public static TridentSync read(FriendlyByteBuf buf) {
        int tridentEntityId = buf.readInt();
        int stuckEntityId = buf.readInt();
        return new TridentSync(tridentEntityId, stuckEntityId);
    }
}
