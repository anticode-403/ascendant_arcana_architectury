package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.block.*;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class AArcanaBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.BLOCK);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BuddingRestorineBlock> BUDDING_RESTORINE = register(
            () -> new BuddingRestorineBlock(BlockBehaviour.Properties.copy(Blocks.BUDDING_AMETHYST).requiresCorrectToolForDrops().explosionResistance(3f).strength(4.5f)),
            "budding_restorine",
            true
    );

    public static final RegistrySupplier<AmethystClusterBlock> SMALL_RESTORINE_BUD = register(
            () -> new AmethystClusterBlock(3, 4, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD).strength(1.5F).lightLevel((state) -> 1)),
            "small_restorine_bud",
            true
    );
    public static final RegistrySupplier<AmethystClusterBlock> MEDIUM_RESTORINE_BUD = register(
            () -> new AmethystClusterBlock(4, 3, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD).strength(1.5F).lightLevel((state) -> 2)),
            "medium_restorine_bud",
            true
    );
    public static final RegistrySupplier<AmethystClusterBlock> LARGE_RESTORINE_BUD = register(
            () -> new AmethystClusterBlock(5, 3, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD).strength(1.5F).lightLevel((state) -> 3)),
            "large_restorine_bud",
            true
    );
    public static final RegistrySupplier<AmethystClusterBlock> RESTORINE_CLUSTER = register(
            () -> new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD).strength(1.5F).lightLevel((state) -> 4)),
            "restorine_cluster",
            true
    );
    public static final RegistrySupplier<AmethystClusterBlock> MASSIVE_RESTORINE_CLUSTER = register(
            () -> new AmethystClusterBlock(9, 2, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD).strength(1.5F).lightLevel((state) -> 5)),
            "massive_restorine_cluster",
            true
    );

    public static final RegistrySupplier<CopperEnchantingTableBlock> COPPER_ENCHANTING_TABLE = register(
            () -> new CopperEnchantingTableBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE).lightLevel((state) -> 0).strength(2.0F, 1200.0F).sound(SoundType.COPPER)),
            "copper_enchanting_table",
            true
    );
    public static final RegistrySupplier<BlockEntityType<CopperEnchantingTableBlockEntity>> COPPER_ENCHANTING_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "copper_enchanting_table",
            () -> BlockEntityType.Builder.of(CopperEnchantingTableBlockEntity::new, COPPER_ENCHANTING_TABLE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "enchanting_table"))
    );

    public static final RegistrySupplier<CrystalizedLavaBlock> CRYSTALIZED_LAVAL_BLOCK = register(
            () -> new CrystalizedLavaBlock(BlockBehaviour.Properties.copy(Blocks.FROSTED_ICE).lightLevel((state) -> 13).friction(0.6F).isValidSpawn((a, b, c, d) -> false)),
            "crystalized_lava",
            false
    );

    public static <T extends Block> RegistrySupplier<T> register(Supplier<T> block, String name, boolean shouldRegisterItem) {
        RegistrySupplier<T> registeredBlock = BLOCKS.register(name, block);
        if (shouldRegisterItem) {
            AArcanaItems.register(() -> new BlockItem(registeredBlock.get(), new Item.Properties()), name);
        }
        return registeredBlock;
    }

    private static ItemStack after(Item item) {
        return new ItemStack(item);
    }

    public static void initialize() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register((itemGroup) -> {
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER);
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.RESTORINE_CLUSTER);
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.LARGE_RESTORINE_BUD);
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.MEDIUM_RESTORINE_BUD);
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.SMALL_RESTORINE_BUD);
//            itemGroup.addAfter(after(Items.AMETHYST_CLUSTER), AArcanaBlocks.BUDDING_RESTORINE);
//        });
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
//            itemGroup.addBefore(after(Items.ENCHANTING_TABLE), AArcanaBlocks.COPPER_ENCHANTING_TABLE);
//        });
    }
}
