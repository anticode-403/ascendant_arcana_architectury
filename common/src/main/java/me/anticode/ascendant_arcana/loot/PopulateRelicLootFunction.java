package me.anticode.ascendant_arcana.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.anticode.ascendant_arcana.init.AArcanaLootFunctionTypes;
import me.anticode.ascendant_arcana.item.RelicItem;
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

public class PopulateRelicLootFunction extends LootItemConditionalFunction{
    final NumberProvider strength;
    final int[] relicTypes;

    protected PopulateRelicLootFunction(LootItemCondition[] conditions, NumberProvider strength, int[] relicTypes) {
        super(conditions);
        this.strength = strength;
        this.relicTypes = relicTypes;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<Relics> relics = Lists.newArrayList();
        int str = strength.getInt(context);
        for (int relicType : this.relicTypes) {
            Relics relic = Relics.fromId(relicType);
            if (RelicHelper.canApplyRelic(stack, relic, str)) relics.add(relic);
        }
        stack.getOrCreateTag().putInt(RelicItem.RELIC_STRENGTH_KEY, str);
        stack.getOrCreateTag().putInt(RelicItem.RELIC_TYPE_KEY, Relics.toId(relics.get(random.nextInt(0, relics.size() - 1))));
        return stack;
    }

    @Override
    public @NotNull LootItemFunctionType getType() {
        return AArcanaLootFunctionTypes.POPULATE_RELIC.get();
    }

    public static Builder builder(NumberProvider strength, int[] relicTypes) {
        return new Builder(strength, relicTypes);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        public final NumberProvider strength;
        public final int[] relicTypes;

        public Builder(NumberProvider strength, int[] relicTypes) {
            this.strength = strength;
            this.relicTypes = relicTypes;
        }

        @Override
        public @NotNull LootItemFunction build() {
            return new PopulateRelicLootFunction(this.getConditions(), this.strength, this.relicTypes);
        }

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PopulateRelicLootFunction> {
        @Override
        public void serialize(JsonObject jsonObject, PopulateRelicLootFunction conditionalLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, conditionalLootFunction, jsonSerializationContext);
            jsonObject.add("strength", jsonSerializationContext.serialize(conditionalLootFunction.strength));
            JsonArray serializedArray = new JsonArray();
            for (int value : conditionalLootFunction.relicTypes) {
                serializedArray.add(value);
            }
            jsonObject.add("relics", serializedArray);
        }

        @Override
        public @NotNull PopulateRelicLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider strength = GsonHelper.convertToObject(jsonObject, "strength", context, NumberProvider.class);
            JsonArray array = GsonHelper.getAsJsonArray(jsonObject, "relics");
            int[] relicTypes = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                relicTypes[i] = array.get(i).getAsInt();
            }
            return new PopulateRelicLootFunction(conditions, strength, relicTypes);
        }
    }
}
