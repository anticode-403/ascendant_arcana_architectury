package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void smeltingMod(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity,
                                    Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SMELTING.get(), stack) > 0) {
            List<ItemStack> drops = cir.getReturnValue();
            if (!drops.isEmpty()) {
                drops = new ArrayList<>(drops);
//                boolean smeltsSelf = state.isIn()
                int dropsSize = drops.size();
                for (int i = 0; i < dropsSize; i++) {
                    Tuple<ItemStack, Float> smelted = getSmeltedStack(level, drops.get(i));
                    if (smelted != null) {
                        level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        drops.set(i, smelted.getA());
                        AbstractFurnaceBlockEntityAccessor.ascendant_arcana$dropExperience(level, entity.position(), 1, smelted.getB());
                    }
                }
                cir.setReturnValue(drops);
            }
        }
    }

    @Unique
    private static Tuple<ItemStack, Float> getSmeltedStack(ServerLevel level, ItemStack stack) {
        for (SmeltingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.test(stack)) {
                    return new Tuple<>(new ItemStack(recipe.getResultItem(level.registryAccess()).getItem(), recipe.getResultItem(level.registryAccess()).getCount() * stack.getCount()), recipe.getExperience());
                }
            }
        }
        return null;
    }
}
