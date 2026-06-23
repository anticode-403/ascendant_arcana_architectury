package me.anticode.ascendant_arcana.forge.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin<T> {
    @Shadow @Final
    private ResourceKey<Registry<T>> key;

    @Shadow
    @NotNull
    public abstract Optional<Holder<T>> getHolder(T value);

    @ModifyReturnValue(method = "getKeys", at = @At("RETURN"), remap = false)
    private @NotNull Set<ResourceLocation> filterOutKeys(@NotNull Set<ResourceLocation> original) {
        if (!key.equals(Registries.ENCHANTMENT))
            return original;
        return original.stream().filter(AArcanaEnchantmentHelper::isEnchantmentEnabled).collect(Collectors.toUnmodifiableSet());
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "getResourceKeys", at = @At("RETURN"), remap = false)
    private Set<ResourceKey<T>> filterOutResourceKeys(@NotNull Set<ResourceKey<T>> original) {
        if (!key.equals(Registries.ENCHANTMENT))
            return original;
        return original.stream().filter(key -> AArcanaEnchantmentHelper.isEnchantmentEnabled(key.location())).collect(Collectors.toUnmodifiableSet());
    }

    @ModifyReturnValue(method = "getValues", at = @At("RETURN"), remap = false)
    private Collection<T> filterOutValues(@NotNull Collection<T> original) {
        if (!key.equals(Registries.ENCHANTMENT))
            return original;
        return original.stream().filter(value -> AArcanaEnchantmentHelper.isEnchantmentEnabled(key.location())).collect(Collectors.toUnmodifiableSet());
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "getEntries", at = @At("RETURN"), remap = false)
    private Set<Map.Entry<ResourceKey<T>, T>> filterEntries(@NotNull Set<Map.Entry<ResourceKey<T>, T>> original) {
        if (!key.equals(Registries.ENCHANTMENT))
            return original;
        return original.stream().filter(value -> AArcanaEnchantmentHelper.isEnchantmentEnabled(key.location())).collect(Collectors.toUnmodifiableSet());
    }

    @SuppressWarnings({"unchecked"})
    @ModifyReturnValue(method = "iterator", at = @At("RETURN"), remap = false)
    private Iterator<T> filterIterator(Iterator<T> original) {
        if (!key.equals(Registries.ENCHANTMENT))
            return original;

        // This is less performant, but we do it this way just in case new values are put into the iterator.
        List<T> list = new ArrayList<>();
        while (original.hasNext()) {
            T it = original.next();
            Optional<Holder<T>> holder = getHolder(it);
            if (holder.isPresent() && AArcanaEnchantmentHelper.isEnchantmentEnabled(ForgeRegistries.ENCHANTMENTS.getKey(((Holder<Enchantment>)holder.get()).value()))) {
                list.add(it);
            }
        }
        return list.iterator();
    }
}
