package me.anticode.ascendant_arcana.logic;

import me.anticode.ascendant_arcana.api.EnchantedArrow;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemHelper {
    public static void forEachEnchantment(Consumer consumer, ItemStack stack, boolean allowEmpty)
    {
        if(!stack.isEmpty() || allowEmpty)
        {
            ListTag listTag = stack.getEnchantmentTags();

            for(int i = 0; i < listTag.size(); ++i)
            {
                String string = listTag.getCompound(i).getString("id");
                int j = listTag.getCompound(i).getInt("lvl");
                BuiltInRegistries.ENCHANTMENT.getOptional(ResourceLocation.tryParse(string)).ifPresent((enchantment)->
                        consumer.accept(enchantment, stack, j));
            }
        }
    }

    public static int getCrossbowMaxArrows(ItemStack crossbowStack) {
        if (!(crossbowStack.getItem() instanceof CrossbowItem)) return 0;
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbowStack) > 0) return 3;
        int repeatingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.REPEATING.get(), crossbowStack);
        int salvoLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SALVO.get(), crossbowStack);
        return salvoLevel != 0 ? 2 + salvoLevel * 2 : repeatingLevel * 2;
    }

    public static void applyPpeRelicsAndEnchantments(AbstractArrow abstractArrow, ItemStack itemStack) {
        double damageMultiplier = 1 + RelicHelper.getTooltipStrength(Relics.DAMAGE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.DAMAGE))*0.01;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);

        if (enchantments.getOrDefault(AArcanaEnchantments.SALVO.get(), 0) != 0) damageMultiplier -= 0.25F;

        int archersGambitLevel = enchantments.getOrDefault(AArcanaEnchantments.ARCHERS_GAMBIT.get(), 0);
        int evokersWrathLevel = enchantments.getOrDefault(AArcanaEnchantments.EVOKERS_WRATH.get(), 0);
        int rejuvenatingShotLevel = enchantments.getOrDefault(AArcanaEnchantments.REJUVENATING_SHOT.get(), 0);
        int ricochetLevel = enchantments.getOrDefault(AArcanaEnchantments.RICOCHET.get(), 0);
        int hobblingShotLevel = enchantments.getOrDefault(AArcanaEnchantments.HOBBLING_SHOT.get(), 0);
        int miasmaLevel = enchantments.getOrDefault(AArcanaEnchantments.MIASMA.get(), 0);

        abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() * damageMultiplier);

        EnchantedArrow enchantedArrow = (EnchantedArrow) abstractArrow;
        enchantedArrow.ascendant_arcana$setArchersGambitLevel(archersGambitLevel);
        enchantedArrow.ascendant_arcana$setEvokersWrathLevel(evokersWrathLevel);
        enchantedArrow.ascendant_arcana$setRejuvenatingShotLevel(rejuvenatingShotLevel);
        enchantedArrow.ascendant_arcana$setRicochetLevel(ricochetLevel);
        enchantedArrow.ascendant_arcana$setHobblingShotLevel(hobblingShotLevel);
        enchantedArrow.ascendant_arcana$setMiasmaLevel(miasmaLevel);
    }

    public static List<AttributeModifier> multiplyAttributeList(List<AttributeModifier> attributes, double multiplier) {
        List<AttributeModifier> newModifiers = new LinkedList<>();
        for (AttributeModifier mod : attributes) {
            double newValue = mod.getAmount() * (1 + multiplier);
            AttributeModifier newMod = new AttributeModifier(mod.getId(), mod.getName(), newValue, mod.getOperation());
            newModifiers.add(newMod);
        }
        return newModifiers;
    }

    @FunctionalInterface
    public interface Consumer
    {
        void accept(Enchantment enchantment, ItemStack stack, int level);
    }
}