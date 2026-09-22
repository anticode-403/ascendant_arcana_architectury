package me.anticode.ascendant_arcana.enchantment.bow;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;

public class Rocketry extends Enchantment {
    public Rocketry() {
        super(Rarity.UNCOMMON, EnchantmentCategory.CROSSBOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return super.checkCompatibility(other) && !(other instanceof MultiShotEnchantment) && !(other instanceof RejuvenatingShot) && !(other instanceof ArrowPiercingEnchantment) && !(other instanceof ArrowInfiniteEnchantment);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }
}
