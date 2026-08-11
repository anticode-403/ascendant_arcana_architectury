package me.anticode.ascendant_arcana.enchantment.armor;

import me.anticode.ascendant_arcana.enchantment.TickableAttributeEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.SwiftSneakEnchantment;

public class Surefoot extends TickableAttributeEnchantment {
    public Surefoot() {
        super(false, Rarity.UNCOMMON, EnchantmentCategory.ARMOR_LEGS, new EquipmentSlot[]{EquipmentSlot.LEGS});
    }

    @Override
    public void initAttributes() {
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, 0.4, AttributeModifier.Operation.ADDITION);
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof SwiftSneakEnchantment) && !(other instanceof Strafe);
    }
}
