package me.anticode.ascendant_arcana.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.EntityPositionSource;

import java.util.ArrayList;
import java.util.List;

public class EntityListSource {
    public static Codec<EntityListSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(new ListCodec<EntityPositionSource>(EntityPositionSource.CODEC).fieldOf("entities").forGetter(source -> source.entities)).apply(instance, EntityListSource::new));

    public final List<EntityPositionSource> entities;

    public EntityListSource(List<EntityPositionSource> entities) {
        this.entities = entities;
    }

    public static EntityListSource read(FriendlyByteBuf friendlyByteBuf) {
        List<EntityPositionSource> entities = new ArrayList<>();
        for (int i = 0; i < friendlyByteBuf.readInt(); i++) {
            entities.add(EntityPositionSource.Type.ENTITY.read(friendlyByteBuf));
        }
        return new EntityListSource(entities);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, EntityListSource entityListSource) {
        friendlyByteBuf.writeInt(entityListSource.entities.size());
        for (EntityPositionSource entityPositionSource : entityListSource.entities) {
            EntityPositionSource.Type.ENTITY.write(friendlyByteBuf, entityPositionSource);
        }
    }

    public Codec<EntityListSource> codec() {
        return EntityListSource.CODEC;
    }
}
