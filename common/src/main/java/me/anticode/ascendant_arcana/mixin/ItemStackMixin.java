package me.anticode.ascendant_arcana.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasTag();

    @Shadow
    @Nullable
    public abstract CompoundTag getTag();

    @Unique
    private static Enchantment replacement = null;

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void enchantmentCapacity(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        if (!AArcanaEnchantmentHelper.testEnchantmentCost(stack, AArcanaEnchantmentHelper.getEnchantmentCost(enchantment, level))) {
            ci.cancel();
        }
    }

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(Enchantment enchantment, int level, CallbackInfo ci) {
        if (!AArcanaEnchantmentHelper.isEnchantmentEnabled(enchantment)) {
            replacement = AArcanaEnchantmentHelper.getReplacement(enchantment, (ItemStack) (Object) this);
            if (replacement == null) {
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "enchant", at = @At("HEAD"), argsOnly = true)
    private Enchantment disableEnchantments(Enchantment value) {
        if (replacement != null) {
            return replacement;
        }
        return value;
    }

    @ModifyVariable(method = "enchant", at = @At("HEAD"), argsOnly = true)
    private int disableEnchantments(int value) {
        if (replacement != null) {
            Enchantment temp = replacement;
            replacement = null;
            return Math.min(temp.getMaxLevel(), value);
        }
        return value;
    }

    @ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
    private int implementDurabilityRelic(int maxDamage) {
        if (AscendantArcana.config.durability_additive) return Mth.floor(maxDamage + RelicHelper.getStrengthFromNbt(Relics.DURABILITY, getTag()));
        else return Mth.floor(maxDamage * (1 + RelicHelper.getStrengthFromNbt(Relics.DURABILITY, getTag())));
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<Attribute, AttributeModifier> implementAttributeRelics(Multimap<Attribute, AttributeModifier> original, @Local(argsOnly = true) EquipmentSlot slot) {
        if (!hasTag()) return original;
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : original.entries()) {
            modifiers.put(entry.getKey(), entry.getValue());
        }
        if (getItem() instanceof ArmorItem armorItem) {
            if (slot != armorItem.getEquipmentSlot()) return modifiers;
            UUID uuid = switch (armorItem.getEquipmentSlot()) {
                case HEAD -> UUID.fromString("ccd7386d-62cf-4ef7-8cc1-a8a2ac7f942c");
                case CHEST -> UUID.fromString("610c3b9b-9c45-4845-8289-99dbe5034894");
                case LEGS -> UUID.fromString("e91f5ebf-3c02-43ec-a842-ce9b68a80c3a");
                case FEET -> UUID.fromString("93ef9100-4f32-45e0-8568-f837918e9b43");
                default -> null;
            };
            float protectionValue = (float) RelicHelper.getStrengthFromNbt(Relics.PROTECTION, getTag());
            if (protectionValue != 0) {
                AttributeModifier modifier = new AttributeModifier(uuid, "Protection Relic Bonus", protectionValue, AttributeModifier.Operation.MULTIPLY_BASE);
                modifiers.put(AArcanaAttributes.PROTECTION.get(), modifier);
            }
        }
        else if (getItem().getMaxDamage() > 1) {
            if (slot != EquipmentSlot.MAINHAND) return modifiers;
            Map<Relics, Integer> relics = RelicHelper.fromNbt(getTag());
            if (relics.isEmpty()) return modifiers;
            if (relics.containsKey(Relics.DAMAGE)) {
                double damageValue = RelicHelper.getStrengthFromNbt(Relics.DAMAGE, getTag());
                List<AttributeModifier> oldDamageModifiers = modifiers.get(Attributes.ATTACK_DAMAGE).stream().toList();
                List<AttributeModifier> newModifiers = ItemHelper.multiplyAttributeList(oldDamageModifiers, damageValue);
                modifiers.replaceValues(Attributes.ATTACK_DAMAGE, newModifiers);
            }
        }
        else if (getItem() instanceof CrossbowItem || getItem() instanceof BowItem || getItem() instanceof TridentItem) {
            if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return modifiers;
            Map<Relics, Integer> relics = RelicHelper.fromNbt(getTag());
            if (relics.isEmpty()) return modifiers;
            if (relics.containsKey(Relics.HASTE)) {
                double hasteValue = RelicHelper.getStrengthFromNbt(Relics.HASTE, getTag()) / 2;
                AttributeModifier modifier = new AttributeModifier(UUID.fromString("f2bb3e62-513f-4804-a194-2965d232c7ad"), "Haste Relic Bonus", hasteValue, AttributeModifier.Operation.MULTIPLY_BASE);
                modifiers.put(AArcanaAttributes.DRAW_SPEED.get(), modifier);
            }
        }
        return modifiers;
    }

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN"))
    private void addRelicTooltipInfo(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (!hasTag()) return;
        Map<Relics, Integer> relics = RelicHelper.fromNbt(getTag());
        if (relics.isEmpty()) return;
        int i = 1;
        tooltip.add(i++, Component.empty());
        tooltip.add(i++, Component.translatable("item.relics.tooltip.on_tool", relics.size(), RelicHelper.getRelicCapacity((ItemStack)(Object)this)).withStyle(ChatFormatting.GRAY));
        for (Map.Entry<Relics, Integer> entry : relics.entrySet()) {
            int visualStrength = RelicHelper.getTooltipStrength(entry.getKey(), entry.getValue());
            Component relicName = Component.translatable("item.relics.type." + entry.getKey().toString().toLowerCase());
            String hasPercent = (entry.getKey() == Relics.HASTE || entry.getKey() == Relics.PROTECTION || entry.getKey() == Relics.DAMAGE || (entry.getKey() == Relics.DURABILITY && !AscendantArcana.config.durability_additive)) ? "%" : "";
            Component line = Component.translatable("item.relics.tooltip", visualStrength, relicName, hasPercent).withStyle(ChatFormatting.BLUE);
            tooltip.add(i++, line);
        }
    }

    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
    private Multimap<Attribute, AttributeModifier> removeProtectionAttributeFromTooltip(ItemStack instance, EquipmentSlot equipmentSlot, Operation<Multimap<Attribute, AttributeModifier>> original) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : original.call(instance, equipmentSlot).entries().stream().filter((entry) -> entry.getKey() != AArcanaAttributes.PROTECTION.get()).collect(Collectors.toSet())) {
            modifiers.put(entry.getKey(), entry.getValue());
        }
        modifiers.removeAll(AArcanaAttributes.PROTECTION.get());
        return modifiers;
    }

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN"))
    private void addEnchantmentCapacityTooltipWhileAdvanced(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        ItemStack itemStack = (ItemStack)(Object)this;
        if (tooltipFlag.isAdvanced() && (itemStack.isEnchanted() || itemStack.isEnchantable() || itemStack.getItem() instanceof EnchantedBookItem)) {
            tooltip.add(Component.translatable("item.enchantment_capacity", AArcanaEnchantmentHelper.getEnchantmentUsage(itemStack), AArcanaEnchantmentHelper.getEnchantmentCapacity(itemStack)));
        }
    }

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN"))
    private void addTreasureEnchantmentInfo(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        if (getItem() instanceof EnchantedBookItem) {
            List<Component> tooltip = cir.getReturnValue();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments((ItemStack)(Object) this);
            boolean hasTreasure = false;
            for (Enchantment enchantment : enchantments.keySet()) {
                if (enchantment.isTreasureOnly() && !enchantment.isCurse()) hasTreasure = true;
            }
            if (hasTreasure) {
                tooltip.add(Component.translatable("item.book_contains_treasure_title").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("item.book_contains_treasure_body").withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
