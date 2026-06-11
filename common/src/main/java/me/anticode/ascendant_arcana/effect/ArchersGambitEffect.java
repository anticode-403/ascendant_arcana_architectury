package me.anticode.ascendant_arcana.effect;


import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ArchersGambitEffect extends MobEffect {
    public ArchersGambitEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xdba213);
        if (AArcanaAttributes.DRAW_SPEED.isPresent()) addAttributeModifier(AArcanaAttributes.DRAW_SPEED.get(), "7851b886-b3dc-4da8-b948-6c896ac9fde4", 0.3d, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}