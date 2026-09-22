package me.anticode.ascendant_arcana.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AllegroEffect extends MobEffect {
    public AllegroEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8803fc);
        addAttributeModifier(Attributes.ATTACK_SPEED, "c44105f3-3f95-4372-86ab-a5acfbffc710", 1.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
