package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.loot.ApplyRelicLootFunction;
import me.anticode.ascendant_arcana.loot.PopulateRelicLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class AArcanaLootFunctionTypes {
    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    public static final RegistrySupplier<LootItemFunctionType> APPLY_RELICS = register("apply_relics", new ApplyRelicLootFunction.Serializer());
    public static final RegistrySupplier<LootItemFunctionType> POPULATE_RELIC = register("populate_relic", new PopulateRelicLootFunction.Serializer());

    private static RegistrySupplier<LootItemFunctionType> register(String id, Serializer<? extends LootItemFunction> jsonSerializer) {
        return LOOT_FUNCTION_TYPES.register(id, () -> new LootItemFunctionType(jsonSerializer));
    }

    public static void initialize() {
        LOOT_FUNCTION_TYPES.register();
    }
}
