package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.api.PotionArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(Arrow.class)
public class ArrowAccessor implements PotionArrow {
    @Shadow
    @Final
    private Set<MobEffectInstance> effects;

    @Shadow
    private Potion potion;

    @Override
    public Set<MobEffectInstance> ascendant_arcana$getEffects() {
        return effects;
    }

    public Potion ascendant_arcana$getPotion() {
        return potion;
    }
}
