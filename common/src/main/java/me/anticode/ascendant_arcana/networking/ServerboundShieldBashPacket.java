package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ServerboundShieldBashPacket(boolean status) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "shield_bash_server");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(status);
        return buf;
    }

    public static ServerboundShieldBashPacket read(FriendlyByteBuf buf) {
        return new ServerboundShieldBashPacket(buf.readBoolean());
    }
}
