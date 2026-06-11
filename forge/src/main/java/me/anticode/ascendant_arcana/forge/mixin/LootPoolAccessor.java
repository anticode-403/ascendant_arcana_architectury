package me.anticode.ascendant_arcana.forge.mixin;

import me.anticode.ascendant_arcana.forge.api.LootPoolAccess;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootPool.class)
public class LootPoolAccessor implements LootPoolAccess {
    @Shadow
    @Final
    LootPoolEntryContainer[] entries;

    @Override
    public LootPoolEntryContainer[] getEntries() {
        return entries;
    }
}
