package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class Shattershot extends Enchantment {
    public Shattershot() {
        super(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.CROSSBOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof RejuvenatingShot) && !(other instanceof ArrowPiercingEnchantment) && !(other instanceof ArrowInfiniteEnchantment) && !(other instanceof Rocketry) && !(other instanceof Blazebolt);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }
}
