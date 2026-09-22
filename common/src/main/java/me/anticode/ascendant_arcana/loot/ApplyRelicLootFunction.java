package me.anticode.ascendant_arcana.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.anticode.ascendant_arcana.init.AArcanaLootFunctionTypes;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.relics.RelicEntry;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ApplyRelicLootFunction extends LootItemConditionalFunction {
    final NumberProvider count;
    final NumberProvider strength;
    final RelicEntry[] relicTypes;

    protected ApplyRelicLootFunction(LootItemCondition[] conditions, NumberProvider count, NumberProvider strength, RelicEntry[] relicTypes) {
        super(conditions);
        this.count = count;
        this.strength = strength;
        this.relicTypes = relicTypes;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<RelicEntry> relics = new ArrayList<>();
        int total = count.getInt(context);
        if (RelicHelper.getRelicCapacity(stack) > total) total = RelicHelper.getRelicCapacity(stack);
        for (int i = 0; i < total; i++) {
            int nextStr = strength.getInt(context);
            for (RelicEntry relic : this.relicTypes) {
                if (RelicHelper.canApplyRelic(stack, relic, nextStr)) relics.add(relic);
            }
            RelicEntry nextRelic = relics.get(random.nextInt(0, relics.size() - 1));
            RelicHelper.infuseRelic(stack, nextRelic, nextStr);
        }
        return stack;
    }

    @Override
    public @NotNull LootItemFunctionType getType() {
        return AArcanaLootFunctionTypes.APPLY_RELICS.get();
    }

    public static Builder builder(NumberProvider count, NumberProvider strength, ResourceLocation[] relicTypes) {
        return new Builder(count, strength, relicTypes);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        final NumberProvider count;
        public final NumberProvider strength;
        public final ResourceLocation[] relicTypes;

        public Builder(NumberProvider count, NumberProvider strength, ResourceLocation[] relicTypes) {
            this.count = count;
            this.strength = strength;
            this.relicTypes = relicTypes;
        }

        @Override
        public @NotNull LootItemFunction build() {
            RelicEntry[] relicTypes = new RelicEntry[this.relicTypes.length];
            for (int i = 0; i < this.relicTypes.length; i++) {
                relicTypes[i] = RelicRegistry.get(this.relicTypes[i]);
            }
            return new ApplyRelicLootFunction(this.getConditions(), this.count, this.strength, relicTypes);
        }

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyRelicLootFunction> {
        @Override
        public void serialize(JsonObject jsonObject, ApplyRelicLootFunction conditionalLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, conditionalLootFunction, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize(conditionalLootFunction.count));
            jsonObject.add("strength", jsonSerializationContext.serialize(conditionalLootFunction.strength));
            JsonArray serializedArray = new JsonArray();
            for (RelicEntry value : conditionalLootFunction.relicTypes) {
                serializedArray.add(value.getType().toString());
            }
            jsonObject.add("relics", serializedArray);
        }

        @Override
        public @NotNull ApplyRelicLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider count = GsonHelper.getAsObject(jsonObject, "count", context, NumberProvider.class);
            NumberProvider strength = GsonHelper.getAsObject(jsonObject, "strength", context, NumberProvider.class);
            JsonArray array = GsonHelper.getAsJsonArray(jsonObject, "relics");
            RelicEntry[] relicTypes = new RelicEntry[array.size()];
            for (int i = 0; i < array.size(); i++) {
                relicTypes[i] = RelicRegistry.get(ResourceLocation.tryParse(array.get(i).getAsString()));
            }
            return new ApplyRelicLootFunction(conditions, count, strength, relicTypes);
        }
    }
}
