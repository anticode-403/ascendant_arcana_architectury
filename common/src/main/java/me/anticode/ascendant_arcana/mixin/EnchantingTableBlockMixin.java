package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentTableBlock.class)
public class EnchantingTableBlockMixin {
    @ModifyReturnValue(method = "getMenuProvider", at = @At("RETURN"))
    private MenuProvider createMenuProvider(MenuProvider original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (original == null) return null;
        else return new SimpleMenuProvider((syncId, inventory, player) -> new AArcanaEnchantingMenu(syncId, inventory, ContainerLevelAccess.create(level, pos)), Component.translatable("container.enchant"));
    }
}
