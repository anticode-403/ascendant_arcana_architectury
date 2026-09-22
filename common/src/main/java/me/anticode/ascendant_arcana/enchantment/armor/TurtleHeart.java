package me.anticode.ascendant_arcana.enchantment.armor;

import com.google.common.collect.Maps;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

public class TurtleHeart extends HeartEnchantment {
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();

    public TurtleHeart() {
        super();
        initAttributes();
    }


    public void initAttributes()
    {
        AttributeModifier entityAttributeModifier = new AttributeModifier(UUID.randomUUID(), this::toString, -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        // I hate Forge
        if (AArcanaAttributes.DAMAGE_TAKEN.isPresent()) {
            this.attributeModifiers.put(AArcanaAttributes.DAMAGE_TAKEN.get(), entityAttributeModifier);
        }
    }

    public boolean addAttributes(LivingEntity entity, ItemStack stack, EquipmentSlot slot, int level)
    {
        return AArcanaEnchantmentHelper.addEnchantmentAttributes(this, attributeModifiers, entity, stack, slot, level);
    }

    public void removeAttributes(LivingEntity entity, EquipmentSlot slot)
    {
        AArcanaEnchantmentHelper.removeEnchantmentAttributes(attributeModifiers, entity, slot);
    }
}
