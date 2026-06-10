package me.anticode.ascendant_arcana.block;


import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CopperEnchantingTableBlockEntity extends EnchantmentTableBlockEntity {

    public CopperEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return AArcanaBlocks.COPPER_ENCHANTING_TABLE_BLOCK_ENTITY.get();
    }
}
