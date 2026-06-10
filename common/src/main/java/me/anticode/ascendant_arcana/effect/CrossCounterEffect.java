package me.anticode.ascendant_arcana.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CrossCounterEffect extends MobEffect {
    public CrossCounterEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00cbd6);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "c44105f3-3f95-4372-86ab-a5acfbffc710", 4D, AttributeModifier.Operation.ADDITION);
    }
}
