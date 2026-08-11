package me.anticode.ascendant_arcana.enchantment.bow;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;

public class Salvo extends Enchantment{
    public Salvo() {
        super(Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof MultiShotEnchantment) && !(other instanceof ArrowPiercingEnchantment) && !(other instanceof Repeating) && !(other instanceof Rocketry);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }
}
