package me.anticode.ascendant_arcana.relics;

import com.google.gson.stream.JsonReader;
import dev.architectury.registry.CreativeTabRegistry;
import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.item.RelicItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;

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

    public static void createCreativeTab() {
        List<Supplier<ItemStack>> relicEntries = new ArrayList<>();
        List<Map.Entry<ResourceLocation, RelicEntry>> relicEntrySet = registrations.entrySet().stream().toList();
        for (int i = 0; i < relicEntrySet.size() * 5; i++) {
            int index = Mth.floor((double) i / 5);
            int strength = i + 1 - (index * 5);
            RelicEntry relicType = relicEntrySet.get(index).getValue();
            relicEntries.add(() -> {
                ItemStack relic = new ItemStack(AArcanaItems.RELIC.get());
                RelicItem.writeRelicData(relic, relicType, strength);
                return relic;
            });
        }
        CreativeTabRegistry.appendStack(AscendantArcana.ASCENDANT_ARCANA_TAB, relicEntries.stream());
    }

    public static void loadRelics(ResourceManager resourceManager) {
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("relics", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(entry.getValue().open()));
                String id = entry.getKey().toString().replace("relics/", "");
                id = id.substring(0, id.lastIndexOf('.'));
                registrations.put(ResourceLocation.tryParse(id), RelicEntry.fromJson(reader));
                createCreativeTab();
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
        createCreativeTab();
    }
}
