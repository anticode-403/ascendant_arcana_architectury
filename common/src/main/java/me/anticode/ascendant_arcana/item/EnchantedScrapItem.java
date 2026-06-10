package me.anticode.ascendant_arcana.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EnchantedScrapItem extends Item {
    public EnchantedScrapItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
