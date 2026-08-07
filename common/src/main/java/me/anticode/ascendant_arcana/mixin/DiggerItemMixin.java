package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Tier;getSpeed()F"))
    public float getSpeed(Tier instance, Operation<Float> original) {
        float base = original.call(instance);
        if (base < 6) return Mth.floor(base * 2F);
        else return Mth.floor(base * 2.75F);
    }
}
