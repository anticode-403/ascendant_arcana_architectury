package me.anticode.ascendant_arcana.fabric.mixin;

import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void createLivingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        if (!AArcanaAttributes.DAMAGE_TAKEN.isPresent()) AArcanaAttributes.initialize();
        cir.getReturnValue().add(AArcanaAttributes.PROTECTION.get()).add(AArcanaAttributes.DAMAGE_TAKEN.get()).add(AArcanaAttributes.DRAW_SPEED.get());
    }
}
