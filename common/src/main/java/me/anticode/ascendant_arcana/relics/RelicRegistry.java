package me.anticode.ascendant_arcana.relics;

import com.google.gson.stream.JsonReader;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RelicRegistry {
    static Map<ResourceLocation, RelicEntry> registrations = new HashMap<>();

    public static void register(ResourceLocation id, RelicEntry entry) {
        registrations.put(id, entry);
    }

    public static Map<ResourceLocation, RelicEntry> getAll() {
        return new HashMap<>(registrations);
    }

    public static RelicEntry get(ResourceLocation id) {
        return registrations.get(id);
    }

    public static ResourceLocation getId(RelicEntry relicEntry) {
        Optional<Map.Entry<ResourceLocation, RelicEntry>> entry = registrations.entrySet().stream().filter(e -> e.getValue() == relicEntry).findFirst();
        return entry.map(Map.Entry::getKey).orElse(null);
    }

    public static void loadRelics(ResourceManager resourceManager) {
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("relics", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(entry.getValue().open()));
                String id = entry.getKey().toString().replace("relics/", "");
                id = id.substring(0, id.lastIndexOf('.'));
                registrations.put(ResourceLocation.tryParse(id), RelicEntry.fromJson(reader));
            } catch (Exception e) {
                System.err.println("Failed to parse: " + entry.getKey());
                e.printStackTrace();
            }
        }
    }

    public static FriendlyByteBuf toNetwork() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeMap(registrations, FriendlyByteBuf::writeResourceLocation, RelicEntry::toNetwork);
        return buf;
    }

    public static void fromNetwork(FriendlyByteBuf buf) {
        registrations = buf.readMap(FriendlyByteBuf::readResourceLocation, RelicEntry::fromNetwork);
    }
}
