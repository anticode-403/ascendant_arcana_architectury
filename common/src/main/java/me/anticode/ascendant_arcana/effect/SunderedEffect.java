package me.anticode.ascendant_arcana.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SunderedEffect extends MobEffect {
    public SunderedEffect() {
        super(MobEffectCategory.HARMFUL, 0x7e14b3);
        addAttributeModifier(Attributes.ARMOR, "c44105f3-3f95-4372-86ab-a5acfbffc710", -0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "c44105f3-3f95-4372-86ab-a5acfbffc710", -0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
