package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(TagEntry.class)
public class TagEntryMixin {
    @Shadow
    @Final
    private ResourceLocation id;

    @Inject(method = "build", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private <T> void disableEnchantments(TagEntry.Lookup<T> lookup, Consumer<T> consumer, CallbackInfoReturnable<Boolean> cir) {
        if (!AArcanaEnchantmentHelper.isEnchantmentEnabled(id) && BuiltInRegistries.ENCHANTMENT.get(id) != null) {
            cir.setReturnValue(true);
        }
    }
}
