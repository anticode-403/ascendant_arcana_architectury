package me.anticode.ascendant_arcana.mixin;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract CompoundTag getOrCreateTag();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasTag();

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
        return maxDamage + RelicHelper.getTooltipStrength(Relics.DURABILITY, RelicHelper.getValueFromNbt(getOrCreateTag(), Relics.DURABILITY));
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<Attribute, AttributeModifier> implementAttributeRelics(Multimap<Attribute, AttributeModifier> original, @Local(argsOnly = true) EquipmentSlot slot) {
        if (getItem() instanceof ArmorItem armorItem) {
            if (slot != armorItem.getEquipmentSlot()) return original;
            UUID uuid = switch (armorItem.getEquipmentSlot()) {
                case HEAD -> UUID.fromString("ccd7386d-62cf-4ef7-8cc1-a8a2ac7f942c");
                case CHEST -> UUID.fromString("610c3b9b-9c45-4845-8289-99dbe5034894");
                case LEGS -> UUID.fromString("e91f5ebf-3c02-43ec-a842-ce9b68a80c3a");
                case FEET -> UUID.fromString("93ef9100-4f32-45e0-8568-f837918e9b43");
                default -> null;
            };
            int protectionValue = RelicHelper.getTooltipStrength(Relics.PROTECTION, RelicHelper.getValueFromNbt(getOrCreateTag(), Relics.PROTECTION));
            if (protectionValue != 0) {
                AttributeModifier modifier = new AttributeModifier(uuid, "Protection Relic Bonus", protectionValue * 0.01, AttributeModifier.Operation.MULTIPLY_BASE);
                original.put(AArcanaAttributes.PROTECTION.get(), modifier);
            }
        }
        else if (getItem() instanceof TieredItem) {
            if (slot != EquipmentSlot.MAINHAND) return original;
            Map<Relics, Integer> relics = RelicHelper.fromNbt(getOrCreateTag());
            if (relics.isEmpty()) return original;
            if (relics.containsKey(Relics.DAMAGE)) {
                double damageValue = RelicHelper.getTooltipStrength(Relics.DAMAGE, relics.get(Relics.DAMAGE))*0.01;
                List<AttributeModifier> oldDamageModifiers = original.get(Attributes.ATTACK_DAMAGE).stream().toList();
                List<AttributeModifier> newModifiers = ItemHelper.multiplyAttributeList(oldDamageModifiers, damageValue);
                original.replaceValues(Attributes.ATTACK_DAMAGE, newModifiers);
            }
        }
        else if (getItem() instanceof CrossbowItem || getItem() instanceof BowItem) {
            if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return original;
            Map<Relics, Integer> relics = RelicHelper.fromNbt(getOrCreateTag());
            if (relics.isEmpty()) return original;
            if (relics.containsKey(Relics.DAMAGE)) {
                // TODO: Fix crossbow and bow relics
                double damageValue = RelicHelper.getTooltipStrength(Relics.DAMAGE, relics.get(Relics.DAMAGE)) * 0.01;
//                List<EntityAttributeModifier> oldDamageModifiers = original.get(EntityAttributes_RangedWeapon.DAMAGE.attribute).stream().toList();
//                List<EntityAttributeModifier> newModifiers = ItemHelper.multiplyAttributeList(oldDamageModifiers, damageValue);
//                original.replaceValues(EntityAttributes_RangedWeapon.DAMAGE.attribute, newModifiers);
            }
            if (relics.containsKey(Relics.HASTE)) {
                double hasteValue = RelicHelper.getTooltipStrength(Relics.HASTE, relics.get(Relics.HASTE)) * 0.005;
//                EntityAttributeModifier modifier = new EntityAttributeModifier(UUID.fromString("f2bb3e62-513f-4804-a194-2965d232c7ad"), "Haste Relic Bonus", hasteValue, EntityAttributeModifier.Operation.MULTIPLY_BASE);
//                original.put(EntityAttributes_RangedWeapon.HASTE.attribute, modifier);
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float applySwiftnessMiningSpeedBonus(float miningSpeedMultiplier) {
        Map<Relics, Integer> relics = RelicHelper.fromNbt(getOrCreateTag());
        if (!relics.containsKey(Relics.HASTE)) return miningSpeedMultiplier;
        float hasteValue = (float)RelicHelper.getTooltipStrength(Relics.HASTE, relics.get(Relics.HASTE));
        return miningSpeedMultiplier *  (1 + (hasteValue * 0.01F));
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", ordinal = 0, shift = At.Shift.AFTER))
    private void addRelicTooltipInfo(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (!hasTag()) return;
        Map<Relics, Integer> relics = RelicHelper.fromNbt(getOrCreateTag());
        if (relics.isEmpty()) return;
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.relics.tooltip.on_tool", relics.size(), RelicHelper.getRelicCapacity((ItemStack)(Object)this)).withStyle(ChatFormatting.GRAY));
        for (Map.Entry<Relics, Integer> entry : relics.entrySet()) {
            int visualStrength = RelicHelper.getTooltipStrength(entry.getKey(), entry.getValue());
            Component relicName = Component.translatable("item.relics.type." + entry.getKey().toString().toLowerCase());
            String hasPercent = (entry.getKey() == Relics.HASTE || entry.getKey() == Relics.PROTECTION || entry.getKey() == Relics.DAMAGE) ? "%" : "";
            Component line = Component.translatable("item.relics.tooltip", visualStrength, relicName, hasPercent).withStyle(ChatFormatting.BLUE);
            tooltip.add(line);
        }
    }

    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
    private Multimap<Attribute, AttributeModifier> removeProtectionAttributeFromTooltip(ItemStack instance, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = instance.getAttributeModifiers(slot);
        modifiers.removeAll(AArcanaAttributes.PROTECTION.get());
        return modifiers;
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z", shift = At.Shift.AFTER))
    private void addEnchantmentCapacityTooltipWhileAdvanced(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        ItemStack itemStack = (ItemStack)(Object)this;
        if (tooltipFlag.isAdvanced() && (itemStack.isEnchanted() || itemStack.isEnchantable() || itemStack.getItem() instanceof EnchantedBookItem)) {
            tooltip.add(Component.translatable("item.enchantment_capacity", AArcanaEnchantmentHelper.getEnchantmentUsage(itemStack), AArcanaEnchantmentHelper.getEnchantmentCapacity(itemStack)));
        }
    }

    @Inject(method = "getTooltipLines", at = @At(value = "TAIL"))
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
