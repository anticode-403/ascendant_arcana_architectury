package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record JoltTargetsPacket(LivingEntity victim, Entity attacker, Entity indirectEntity, int chainLength) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "jolt_sync");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(victim.getId());
        buf.writeInt(attacker.getId());
        buf.writeBoolean(indirectEntity != null);
        if (indirectEntity != null) buf.writeInt(indirectEntity.getId());
        buf.writeInt(chainLength);
        return buf;
    }

    public static JoltTargetsPacket read(FriendlyByteBuf buf, ServerLevel level) {
        int victimId = buf.readInt();
        int attackerId = buf.readInt();
        int indirectEntityId = -1;
        if (buf.readBoolean()) indirectEntityId = buf.readInt();
        int chainLength = buf.readInt();
        return new JoltTargetsPacket((LivingEntity) level.getEntity(victimId), level.getEntity(attackerId), indirectEntityId != -1 ? level.getEntity(indirectEntityId) : null, chainLength);
    }
}
