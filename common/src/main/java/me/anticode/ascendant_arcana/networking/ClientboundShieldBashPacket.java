package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundShieldBashPacket(UUID playerId, boolean status) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "shield_bash_client");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(playerId);
        buf.writeBoolean(status);
        return buf;
    }

    public static ClientboundShieldBashPacket read(FriendlyByteBuf buf) {
        return new ClientboundShieldBashPacket(buf.readUUID(), buf.readBoolean());
    }
}
