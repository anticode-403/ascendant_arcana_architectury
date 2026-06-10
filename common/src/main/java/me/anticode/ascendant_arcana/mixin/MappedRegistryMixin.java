package me.anticode.ascendant_arcana.mixin;

import com.mojang.serialization.Lifecycle;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.RemovedRegistryEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {
    @Shadow
    public abstract HolderOwner<T> holderOwner();

    @Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(int i, ResourceKey<T> registryKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        if ((Object) this == BuiltInRegistries.ENCHANTMENT && !AArcanaEnchantmentHelper.isEnchantmentEnabled(registryKey.location())) {
            RemovedRegistryEntry.REMOVED_ENTRIES.add(new RemovedRegistryEntry((Enchantment) object, registryKey.location(), i));
            cir.setReturnValue(Holder.Reference.createStandAlone(holderOwner(), registryKey));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(@Nullable ResourceKey<T> key, CallbackInfoReturnable<@Nullable T> cir) {
        if (key != null && (Object) this == BuiltInRegistries.ENCHANTMENT) {
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromId(key.location());
            if (removedEntry != null) {
                cir.setReturnValue((T) removedEntry.enchantment());
            }
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(@Nullable ResourceLocation id, CallbackInfoReturnable<@Nullable T> cir) {
        if (id != null && (Object) this == BuiltInRegistries.ENCHANTMENT) {
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromId(id);
            if (removedEntry != null) {
                cir.setReturnValue((T) removedEntry.enchantment());
            }
        }
    }

    @Inject(method = "getKey", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(T value, CallbackInfoReturnable<@Nullable ResourceLocation> cir) {
        if ((Object) this == BuiltInRegistries.ENCHANTMENT) {
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromEnchantment((Enchantment) value);
            if (removedEntry != null) {
                cir.setReturnValue(removedEntry.identifier());
            }
        }
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    private void disableEnchantmentsRawid(@Nullable T value, CallbackInfoReturnable<Integer> cir) {
        if (value != null && (Object) this == BuiltInRegistries.ENCHANTMENT) {
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromEnchantment((Enchantment) value);
            if (removedEntry != null) {
                cir.setReturnValue(removedEntry.rawId());
            }
        }
    }
}
