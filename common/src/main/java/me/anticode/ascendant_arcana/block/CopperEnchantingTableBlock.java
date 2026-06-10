package me.anticode.ascendant_arcana.block;

import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CopperEnchantingTableBlock extends EnchantmentTableBlock {
    public CopperEnchantingTableBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperEnchantingTableBlockEntity(pos, state);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnchantmentTableBlockEntity) {
            Component text = ((Nameable)blockEntity).getDisplayName();
            return new SimpleMenuProvider((syncId, inventory, player) -> new AArcanaEnchantingMenu(syncId, inventory, ContainerLevelAccess.create(level, pos)), text);
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? createTickerHelper(type, AArcanaBlocks.COPPER_ENCHANTING_TABLE_BLOCK_ENTITY.get(), CopperEnchantingTableBlockEntity::bookAnimationTick) : null;
    }
}
