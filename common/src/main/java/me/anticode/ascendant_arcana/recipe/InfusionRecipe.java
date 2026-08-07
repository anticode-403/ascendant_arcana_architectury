package me.anticode.ascendant_arcana.recipe;

import com.google.gson.JsonObject;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import me.anticode.ascendant_arcana.init.AArcanaTags;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.relics.RelicEntry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InfusionRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    public final ResourceLocation templateId;
    public final int maxTier;

    InfusionRecipe(ResourceLocation id, ResourceLocation templateId, int maxTier) {
        this.id = id;
        this.templateId = templateId;
        this.maxTier = maxTier;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.getItem().arch$registryName().equals(templateId);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        if (stack.is(AArcanaTags.Items.INFUSION_BLACKLIST)) return false;
        if (stack.isEnchantable() || stack.isDamageableItem() || stack.getItem() instanceof ArmorItem || stack.getItem() instanceof TieredItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof TridentItem) {
            if (!stack.hasTag()) return true;
            return RelicHelper.fromNbt(stack.getTag()).size() < RelicHelper.getRelicCapacity(stack);
        }
        return false;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.is(AArcanaTags.Items.RELICS) && RelicItem.getRelicStrength(stack) <= maxTier;
    }

    @Override
    public boolean matches(Container inventory, Level level) {
        if (!(this.isTemplateIngredient(inventory.getItem(0)) && this.isBaseIngredient(inventory.getItem(1)) && this.isAdditionIngredient(inventory.getItem(2)))) return false;
        ItemStack baseStack = inventory.getItem(1);
        ItemStack relicStack = inventory.getItem(2);
        return matches(baseStack, relicStack);
    }

    public boolean matches (ItemStack baseStack, ItemStack relicStack) {
        if (baseStack.is(AArcanaTags.Items.INFUSION_BLACKLIST)) return false;
        Map<RelicEntry, Integer> relicMap = RelicHelper.fromNbt(baseStack.getTag());
        RelicEntry relicType = RelicItem.getRelicType(relicStack);
        if (relicMap.size() < RelicHelper.getRelicCapacity(baseStack)) {
            if (relicType.getTarget() == RelicEntry.Target.durability && baseStack.isDamageableItem()) return true;
            if (relicType.getTarget() == RelicEntry.Target.enchantable && (baseStack.isEnchantable() || baseStack.isEnchanted())) return true;
            else if (relicType.getTarget() == RelicEntry.Target.tool && (baseStack.getItem() instanceof TieredItem || baseStack.getItem() instanceof BowItem || baseStack.getItem() instanceof CrossbowItem || baseStack.getItem() instanceof TridentItem || !baseStack.getItem().getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).isEmpty())) return true;
            else return relicType.getTarget() == RelicEntry.Target.armor && baseStack.getItem() instanceof ArmorItem;
        }
        else if (relicStack.getItem() instanceof  RelicItem) {
            if (relicMap.containsKey(relicType)) {
                return RelicItem.getRelicStrength(relicStack) > relicMap.get(relicType);
            }
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        return getOutput(inventory.getItem(1), inventory.getItem(2));
    }

    public ItemStack getOutput(ItemStack baseStack, ItemStack relicStack) {
        ItemStack newStack = baseStack.copy();
        int relicStrength = RelicItem.getRelicStrength(relicStack);
        RelicEntry relicType = RelicItem.getRelicType(relicStack);
        Map<RelicEntry, Integer> relicsMap = RelicHelper.fromNbt(newStack.getTag());
        relicsMap.put(relicType, relicStrength);
        newStack.getOrCreateTag().put(RelicHelper.RELICS_KEY, RelicHelper.toNbt(relicsMap));
        return newStack;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AArcanaRecipes.INFUSION_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        public @NotNull InfusionRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new InfusionRecipe(id, ResourceLocation.tryParse(json.get("template_id").getAsString()), json.get("max_tier").getAsInt());
        }

        public @NotNull InfusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new InfusionRecipe(id, buf.readResourceLocation(), buf.readInt());
        }

        public void toNetwork(FriendlyByteBuf buf, InfusionRecipe recipe) {
            buf.writeResourceLocation(recipe.templateId);
            buf.writeInt(recipe.maxTier);
        }
    }
}
