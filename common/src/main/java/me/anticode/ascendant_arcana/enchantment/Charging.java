package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class Charging extends Enchantment {
    public Charging() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEARABLE, new EquipmentSlot[]{});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HorseArmorItem;
    }
}
