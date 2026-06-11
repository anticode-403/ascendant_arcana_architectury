package me.anticode.ascendant_arcana.forge.mixin;

import me.anticode.ascendant_arcana.forge.api.LootTableAccess;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LootTable.class)
public class LootTableAccessor implements LootTableAccess {

    @Shadow
    @Final
    private List<LootPool> pools;

    @Override
    public List<LootPool> ascendant_arcana$getLootPools() {
        return pools;
    }
}
