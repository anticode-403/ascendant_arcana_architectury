package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.api.ItemEntryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootItem.class)
public class LootItemAccessor implements ItemEntryAccess {
    @Final
    @Shadow
    Item item;

    @Override
    public Item ascendantArcana$getItem() {
        return this.item;
    }
}
