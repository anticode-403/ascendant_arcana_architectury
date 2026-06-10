package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class EnfeeblementCurse extends TickableAttributeEnchantment
{
    public EnfeeblementCurse()
    {
        super(true, Rarity.UNCOMMON, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public void initAttributes()
    {
        addAttributeModifier(Attributes.MAX_HEALTH, -2, AttributeModifier.Operation.ADDITION);
    }
}