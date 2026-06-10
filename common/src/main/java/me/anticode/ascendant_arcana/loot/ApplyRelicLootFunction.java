package me.anticode.ascendant_arcana.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.anticode.ascendant_arcana.init.AArcanaLootFunctionTypes;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApplyRelicLootFunction extends LootItemConditionalFunction {
    final NumberProvider count;
    final NumberProvider strength;
    final int[] relicTypes;

    protected ApplyRelicLootFunction(LootItemCondition[] conditions, NumberProvider count, NumberProvider strength, int[] relicTypes) {
        super(conditions);
        this.count = count;
        this.strength = strength;
        this.relicTypes = relicTypes;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<Relics> relics = Lists.newArrayList();
        int total = count.getInt(context);
        if (RelicHelper.getRelicCapacity(stack) > total) total = RelicHelper.getRelicCapacity(stack);
        for (int i = 0; i < total; i++) {
            int nextStr = strength.getInt(context);
            for (int relicType : this.relicTypes) {
                Relics relic = Relics.fromId(relicType);
                if (RelicHelper.canApplyRelic(stack, relic, nextStr)) relics.add(relic);
            }
            Relics nextRelic = relics.get(random.nextInt(0, relics.size() - 1));
            RelicHelper.applyRelic(stack, nextRelic, nextStr);
        }
        return stack;
    }

    @Override
    public @NotNull LootItemFunctionType getType() {
        return AArcanaLootFunctionTypes.APPLY_RELICS.get();
    }

    public static Builder builder(NumberProvider count, NumberProvider strength, int[] relicTypes) {
        return new Builder(count, strength, relicTypes);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        final NumberProvider count;
        public final NumberProvider strength;
        public final int[] relicTypes;

        public Builder(NumberProvider count, NumberProvider strength, int[] relicTypes) {
            this.count = count;
            this.strength = strength;
            this.relicTypes = relicTypes;
        }

        @Override
        public @NotNull LootItemFunction build() {
            return new ApplyRelicLootFunction(this.getConditions(), this.count, this.strength, this.relicTypes);
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
            for (int value : conditionalLootFunction.relicTypes) {
                serializedArray.add(value);
            }
            jsonObject.add("relics", serializedArray);
        }

        @Override
        public @NotNull ApplyRelicLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider count = GsonHelper.getAsObject(jsonObject, "count", context, NumberProvider.class);
            NumberProvider strength = GsonHelper.getAsObject(jsonObject, "strength", context, NumberProvider.class);
            JsonArray array = GsonHelper.getAsJsonArray(jsonObject, "relics");
            int[] relicTypes = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                relicTypes[i] = array.get(i).getAsInt();
            }
            return new ApplyRelicLootFunction(conditions, count, strength, relicTypes);
        }
    }
}
