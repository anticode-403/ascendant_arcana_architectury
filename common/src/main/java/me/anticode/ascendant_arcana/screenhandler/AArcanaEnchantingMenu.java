package me.anticode.ascendant_arcana.screenhandler;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AArcanaEnchantingMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerLevelAccess context;
    public final int[] enchantmentPower = new int[] { 0 };
    public List<Enchantment> unlockedTreasures = new ArrayList<>();
    public Player player;
    public EnchantmentRecipe recipe;
    ItemStack last;

    public AArcanaEnchantingMenu(int Id, Inventory playerInventory) {
        this(Id, playerInventory, ContainerLevelAccess.NULL);
    }

    public AArcanaEnchantingMenu(int Id, Inventory playerInventory, ContainerLevelAccess context) {
        super(AArcanaMenus.ENCHANTING.get(), Id);

        this.context = context;

        inventory = new SimpleContainer(4) {
            public void setChanged() {
                super.setChanged();
                AArcanaEnchantingMenu.this.slotsChanged(this);
            }
        };
        player = playerInventory.player;
        inventory.startOpen(player);

        addSlot(new EnchantableToolSlot(inventory, 0, 22, 61));
        addSlot(new MagicalScrapSlot(inventory, 1, 164, 60));
        addSlot(new EnchantmentIngredientSlot(inventory, 2, 164, 78));
        addSlot(new EnchantmentIngredientSlot(inventory, 3, 164, 96));

        addDataSlot(DataSlot.shared(enchantmentPower, 0));

        updatePower();

        int x, y;
        for (y = 0; y < 3; ++y) {
            for (x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 36 + x * 18, 137 + y * 18));
            }
        }
        for (y = 0; y < 9; y++) {
            this.addSlot(new Slot(playerInventory, y, 36 + y * 18, 195));
        }
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory != this.inventory) return;
        ItemStack itemStack = inventory.getItem(0);
        if (last == null || itemStack == ItemStack.EMPTY || (last != ItemStack.EMPTY && !last.is(itemStack.getItem()))) {
            dumpContents(true);
            if (last != null && (itemStack.isEmpty() || (!itemStack.isEnchanted() && !itemStack.isEnchantable()))) return;
            last = itemStack;
            updatePower();
        }
    }

    private void updatePower() {
        context.execute((level, pos) -> {
            int i = 0;

            for (BlockPos blockPos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockPos)) {
                    if (level.getBlockEntity(pos.offset(blockPos), BlockEntityType.CHISELED_BOOKSHELF).isPresent()) {
                        ChiseledBookShelfBlockEntity chiseledBookshelf = (ChiseledBookShelfBlockEntity) level.getBlockEntity(pos.offset(blockPos));
                        assert chiseledBookshelf != null;
                        for (int j = 0; j < chiseledBookshelf.getContainerSize(); j++) {
                            ItemStack itemStack1 = chiseledBookshelf.getItem(j);
                            for (Map.Entry<Enchantment, Integer> enchantInstance : EnchantmentHelper.getEnchantments(itemStack1).entrySet()) {
                                int rarityMultiplier = switch (enchantInstance.getKey().getRarity()) {
                                    case UNCOMMON -> 2;
                                    case RARE -> 3;
                                    case VERY_RARE -> 5;
                                    default -> 1;
                                };
                                i += enchantInstance.getValue() * rarityMultiplier;
                                if ((enchantInstance.getKey().isTreasureOnly() || AscendantArcana.config.books_remove_scrap_cost || AscendantArcana.config.books_tier_bypass != 0) && !unlockedTreasures.contains(enchantInstance.getKey())) {
                                    unlockedTreasures.add(enchantInstance.getKey());
                                }
                            }
                        }
                    } else i++;
                }
            }
            NetworkManager.sendToPlayer((ServerPlayer) player, EnchantingScreenSync.Id, new EnchantingScreenSync(containerId, unlockedTreasures).write());
            if (level.getBlockState(pos).getBlock().defaultBlockState().is(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get())) {
                if (i > AscendantArcana.config.uncommon_enchanting_power) i = AscendantArcana.config.uncommon_enchanting_power;
            }
            enchantmentPower[0] = i;
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack moveStack = slot.getItem();
            stackCopy = moveStack.copy();
            if (index <= 3) {
                if (!this.moveItemStackTo(moveStack, 4, 40, true)) return ItemStack.EMPTY;
            } else if (moveStack.isEnchantable() || moveStack.isEnchanted() || moveStack.is(Items.BOOK)) {
                if (!this.moveItemStackTo(moveStack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (moveStack.is(AArcanaItems.ENCHANTED_SCRAP.get())) {
                if (!this.moveItemStackTo(moveStack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(moveStack, 2, 4, false)) return ItemStack.EMPTY;
            }

            if (moveStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (moveStack.getCount() == stackCopy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, moveStack);
        }
        return stackCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        dumpContents(false);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack itemStack = inventory.getItem(0);
        if (recipe == null) return false;
        if (!recipe.enchantment.canEnchant(itemStack) && !(itemStack.is(Items.BOOK) || itemStack.is(Items.ENCHANTED_BOOK))) return false;
        ItemStack scrapStack = inventory.getItem(1);
        ItemStack primaryStack = inventory.getItem(2);
        ItemStack secondaryStack = inventory.getItem(3);

        // Verifying
        if (!AscendantArcana.config.disable_xp && recipe.levelCost > player.experienceLevel) return false;
        if (!AArcanaEnchantmentHelper.testEnchantmentCost(itemStack, AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))) return false;
        if (!AscendantArcana.config.books_remove_scrap_cost || !unlockedTreasures.contains(recipe.enchantment)) {
            if (!scrapStack.is(AArcanaItems.ENCHANTED_SCRAP.get())) return false;
            if (scrapStack.getCount() < recipe.magicalScrapCost) return false;
        }
        if (recipe.primaryIngredientStack != null) {
            if (!recipe.primaryIngredientStack.getIngredient().test(primaryStack)) return false;
            if (recipe.primaryIngredientStack.getCount() > primaryStack.getCount()) return false;
        }
        if (recipe.secondaryIngredientStack != null) {
            if (!recipe.secondaryIngredientStack.getIngredient().test(secondaryStack)) return false;
            if (recipe.secondaryIngredientStack.getCount() > secondaryStack.getCount()) return false;
        }
        Map<Enchantment, Integer> itemEnchants = EnchantmentHelper.getEnchantments(itemStack);
        if (itemEnchants.containsKey(recipe.enchantment)) {
            if (itemEnchants.get(recipe.enchantment) + 1 > recipe.enchantment.getMaxLevel()) return false;
        } else if (!EnchantmentHelper.isEnchantmentCompatible(itemEnchants.keySet(), recipe.enchantment)) return false;

        context.execute((level, pos) -> {
            player.onEnchantmentPerformed(itemStack, recipe.levelCost);
            if (!AscendantArcana.config.books_remove_scrap_cost || !unlockedTreasures.contains(recipe.enchantment))
                scrapStack.setCount(scrapStack.getCount() - recipe.magicalScrapCost);
            ItemStack newStack = itemStack;
            if (itemStack.is(Items.BOOK)) {
                getSlot(0).set(new ItemStack(Items.ENCHANTED_BOOK));
                newStack = getSlot(0).getItem();
            }
            if (scrapStack.isEmpty()) {
                inventory.setItem(1, ItemStack.EMPTY);
            }
            if (recipe.primaryIngredientStack != null) {
                primaryStack.setCount(primaryStack.getCount() - recipe.primaryIngredientStack.getCount());
                if (primaryStack.isEmpty()) {
                    inventory.setItem(2, ItemStack.EMPTY);
                }
            }
            if (recipe.secondaryIngredientStack != null) {
                secondaryStack.setCount(secondaryStack.getCount() - recipe.secondaryIngredientStack.getCount());
                if (secondaryStack.isEmpty()) {
                    inventory.setItem(3, ItemStack.EMPTY);
                }
            }
            if (itemEnchants.containsKey(recipe.enchantment)) {
                itemEnchants.put(recipe.enchantment, itemEnchants.get(recipe.enchantment) + 1);
                EnchantmentHelper.setEnchantments(itemEnchants, newStack);
            } else {
                newStack.enchant(recipe.enchantment, 1);
            }
            player.awardStat(Stats.ENCHANT_ITEM);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, newStack, recipe.levelCost);
            }

            inventory.setChanged();
            slotsChanged(inventory);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        });

        return true;
    }

    public void dumpContents(boolean skipFirst) {
        if (inventory.isEmpty()) return;
        for (int i = skipFirst ? 1 : 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != ItemStack.EMPTY) {
                if (!this.moveItemStackTo(itemStack, 4, 40, true)) player.drop(itemStack, true);
            }
        }
    }

    private static class EnchantableToolSlot extends Slot {
        public EnchantableToolSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.isEnchantable() || stack.isEnchanted() || stack.is(Items.BOOK);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private class MagicalScrapSlot extends Slot {
        public MagicalScrapSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(AArcanaItems.ENCHANTED_SCRAP.get()) && AArcanaEnchantingMenu.this.recipe != null;
        }
    }

    private class EnchantmentIngredientSlot extends Slot {
        public EnchantmentIngredientSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            EnchantmentRecipe recipe = AArcanaEnchantingMenu.this.recipe;
            if (recipe == null) return false;
            int index = getContainerSlot();
            return index == 2 ? recipe.primaryIngredientStack != null : recipe.secondaryIngredientStack != null;
        }
    }
}
