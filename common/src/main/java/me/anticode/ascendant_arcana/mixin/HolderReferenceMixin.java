package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.logic.RemovedRegistryEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Holder.Reference.class)
public abstract class HolderReferenceMixin<T> {
    @Shadow
    @Nullable
    private ResourceKey<T> key;

    @Shadow
    public abstract boolean canSerializeIn(HolderOwner<T> owner);

    @Inject(method = "value", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(CallbackInfoReturnable<T> cir) {
        if (key != null && canSerializeIn((HolderOwner<T>) BuiltInRegistries.ENCHANTMENT.holderOwner())) {
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromId(key.location());
            if (removedEntry != null) {
                cir.setReturnValue((T) removedEntry.enchantment());
            }
        }
    }
}
