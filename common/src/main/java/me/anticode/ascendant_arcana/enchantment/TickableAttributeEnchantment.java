package me.anticode.ascendant_arcana.enchantment;

import com.google.common.collect.Maps;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class TickableAttributeEnchantment extends Enchantment {
    private final boolean isCurse;
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
    private final EquipmentSlot[] slotTypes;

    protected TickableAttributeEnchantment(boolean isCurse, Rarity weight, EnchantmentCategory target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
        this.slotTypes = slotTypes;
        this.isCurse = isCurse;
        initAttributes();
    }

    public void initAttributes()
    {

    }

    public void onTick(LivingEntity entity, ItemStack stack, int level, EquipmentSlot slot)
    {

    }

    @Override
    public boolean isCurse() {
        return isCurse;
    }

    @Override
    public boolean isTreasureOnly() {
        return isCurse;
    }

    protected void addAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        AttributeModifier entityAttributeModifier = new AttributeModifier(UUID.randomUUID(), this::toString, amount, operation);
        this.attributeModifiers.put(attribute, entityAttributeModifier);
    }

    public boolean addAttributes(LivingEntity entity, ItemStack stack, EquipmentSlot slot, int level) {
        if (Arrays.stream(slotTypes).toList().contains(slot)) return false;
        return AArcanaEnchantmentHelper.addEnchantmentAttributes(this, attributeModifiers, entity, stack, slot, level);
    }

    public void removeAttributes(LivingEntity entity, EquipmentSlot slot) {
        AArcanaEnchantmentHelper.removeEnchantmentAttributes(this.attributeModifiers, entity, slot);
    }
}
