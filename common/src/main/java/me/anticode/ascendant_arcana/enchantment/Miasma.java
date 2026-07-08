package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class Miasma extends Enchantment {
    public Miasma() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public boolean isCurse() {
        return false;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof ArrowInfiniteEnchantment) && !(other instanceof Ricochet);
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }
}
