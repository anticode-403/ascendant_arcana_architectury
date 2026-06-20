package me.anticode.ascendant_arcana.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.anticode.ascendant_arcana.logic.RemovedRegistryEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantRandomlyFunction.Serializer.class)
public class EnchantRandomlyFunctionSerializerMixin {
    @ModifyVariable(method = "deserialize(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;[Lnet/minecraft/world/level/storage/loot/predicates/LootItemCondition;)Lnet/minecraft/world/level/storage/loot/functions/EnchantRandomlyFunction;", at = @At("HEAD"), argsOnly = true)
    private JsonObject skipEnchantmentIfDisabled(JsonObject jsonObject) {
        if (jsonObject.has("enchantments")) {
            List<JsonElement> removedEntries = new ArrayList<>();
            for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "enchantments")) {
                String string = GsonHelper.convertToString(jsonElement, "enchantment");
                if (RemovedRegistryEntry.getFromId(new ResourceLocation(string)) != null || BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(string)).isEmpty()) removedEntries.add(jsonElement);
            }
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "enchantments");
            if (removedEntries.isEmpty()) return jsonObject;
            for (JsonElement jsonElement : removedEntries) {
                jsonArray.remove(jsonElement);
            }
            jsonObject.add("enchantments", jsonArray);
        }
        return jsonObject;
    }
}
