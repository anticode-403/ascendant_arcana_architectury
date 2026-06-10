package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.api.LeafEntryAccess;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootPoolSingletonContainer.class)
public class LootPoolSingletonContainerAccessor implements LeafEntryAccess {
    @Shadow
    @Final
    protected int weight;

    @Shadow
    @Final
    protected int quality;

    @Shadow
    @Final
    protected LootItemFunction[] functions;

    @Override
    public int ascendantArcana$getWeight() {
        return this.weight;
    }

    @Override
    public int ascendantArcana$getQuality() {
        return this.quality;
    }

    @Override
    public LootItemFunction[] ascendantArcana$getFunctions() {
        return this.functions;
    }
}
