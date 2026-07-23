package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.TridentRiptideEnchantment;

public class Singularity extends Enchantment {
    public Singularity() {
        super(Rarity.RARE, EnchantmentCategory.TRIDENT, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return super.canEnchant(stack);
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof TridentRiptideEnchantment) && !(other instanceof Sundering) && !(other instanceof Lifetide);
    }
}
