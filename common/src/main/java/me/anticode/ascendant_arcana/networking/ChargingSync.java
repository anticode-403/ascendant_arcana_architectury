package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


public record ChargingSync(int horseId, boolean status) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "charging_sync");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(horseId);
        buf.writeBoolean(status);
        return buf;
    }

    public static ChargingSync read(FriendlyByteBuf buf) {
        return new ChargingSync(buf.readInt(), buf.readBoolean());
    }
}
