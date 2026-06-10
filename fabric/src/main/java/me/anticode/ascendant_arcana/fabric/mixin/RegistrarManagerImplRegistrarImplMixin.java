package me.anticode.ascendant_arcana.fabric.mixin;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.fabric.RegistrarManagerImpl;
import me.anticode.ascendant_arcana.logic.RemovedRegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistrarManagerImpl.RegistrarImpl.class)
public abstract class RegistrarManagerImplRegistrarImplMixin<T> implements Registrar<T> {
    @Shadow
    private Registry<T> delegate;

    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(ResourceLocation id, CallbackInfoReturnable<Boolean> cir) {
        if (delegate == BuiltInRegistries.ENCHANTMENT && RemovedRegistryEntry.getFromId(id) != null) {
            cir.setReturnValue(true);
        }
    }
}
