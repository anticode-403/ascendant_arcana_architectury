package me.anticode.ascendant_arcana.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HobbledEffect extends MobEffect {
    public HobbledEffect() {
        super(MobEffectCategory.HARMFUL, 0x7e14b3);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "c44105f3-3f95-4372-86ab-a5acfbffc710", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
