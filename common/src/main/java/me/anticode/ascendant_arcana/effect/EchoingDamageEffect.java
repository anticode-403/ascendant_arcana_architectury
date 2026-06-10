package me.anticode.ascendant_arcana.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EchoingDamageEffect extends MobEffect {
    public EchoingDamageEffect() {
        super(MobEffectCategory.HARMFUL, 0x07522f);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().magic(), amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int k = 20 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        } else {
            return true;
        }
    }
}
